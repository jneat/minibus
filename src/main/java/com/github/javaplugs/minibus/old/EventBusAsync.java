/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 by rumatoest at github.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package com.github.javaplugs.minibus.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Async event bus that will run each event/handler call in separate thread.
 * By default using CachedThreadPool to run handlers.
 */
@Deprecated
public class EventBusAsync<E extends Event> implements EventBus<E> {

    private static final Logger logger = LoggerFactory.getLogger(EventBusAsync.class);

    private final Thread eventQueueThread;

    private final Queue<E> eventsQueue = new ConcurrentLinkedQueue<>();

    private final ReferenceQueue gcQueue = new ReferenceQueue();

    private final Set<WeakHandler> handlers = Collections.newSetFromMap(new ConcurrentHashMap<WeakHandler, Boolean>());

    private final ExecutorService handlersExecutor;

    /**
     * Create new EventBus instance with default presets.
     */
    public EventBusAsync() {
        this(Executors.newCachedThreadPool());
    }

    /**
     * Create instance with customer ExecutorService for event handlers.
     *
     * @param handlersExecutor Will be used to run event handler processing for each event
     */
    public EventBusAsync(ExecutorService handlersExecutor) {
        this.handlersExecutor = handlersExecutor;
        eventQueueThread = new Thread(this::eventsQueue, "EventQueue handlers thread");
        eventQueueThread.setDaemon(true);
        eventQueueThread.start();
    }

    @Override
    public void subscribe(EventHandler<E> subscriber) {
        handlers.add(new WeakHandler(subscriber, gcQueue));
    }

    @Override
    public void unsubscribe(EventHandler<E> subscriber) {
        handlers.remove(new WeakHandler(subscriber, gcQueue));
    }

    @Override
    public void publish(E event) {
        if (event == null) {
            return;
        }
        event.lock();
        eventsQueue.add(event);
    }

    @Override
    public boolean hasPendingEvents() {
        return !eventsQueue.isEmpty();
    }

    private void eventsQueue() {
        while (true) {
            WeakHandler wh;
            while ((wh = (WeakHandler)gcQueue.poll()) != null) {
                handlers.remove(wh);
            }

            E event = eventsQueue.poll();
            if (event != null) {
                notifySubscribers(event);
            }
        }
    }

    private void notifySubscribers(E event) {
        for (WeakHandler wh : handlers) {
            EventHandler eh = wh.get();
            if (eh == null) {
                continue;
            }

            try {
                if (eh.getType() == null) {
                    if (eh.canHandle(event.getType())) {
                        handlersExecutor.submit(() -> {
                            runHandler(eh, event);
                        });
                    }
                } else if (eh.getType().equals(event.getType())) {
                    handlersExecutor.submit(() -> {
                        runHandler(eh, event);
                    });
                }
            } catch (Throwable th) {
                logger.error("Handler notify fail on event " + event.getType() + ". " + th.getMessage(), th);
            }
        }
    }

    private void runHandler(EventHandler eh, E event) {
        try {
            eh.handle(event);
        } catch (Throwable th) {
            logger.error("Handler fail on event " + event.getType() + ". " + th.getMessage(), th);
        }
    }
}
