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
package com.github.jneat.minibus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Async event bus that will run each event/handler call in separate thread. By default using CachedThreadPool to run
 * handlers.
 */
public class EventBusAsync<E extends EventBusEvent, H extends EventBusHandler<?>> implements EventBus<E, H> {

    private static final Logger logger = LoggerFactory.getLogger(EventBusAsync.class);

    private final Queue<EventWrapper<E, H>> eventsQueue = new ConcurrentLinkedQueue<>();

    private final ReferenceQueue<H> gcQueue = new ReferenceQueue<>();

    private final Map<Class<?>, Set<WeakHandler<H>>> handlersCls = new ConcurrentHashMap<>();

    private final Set<WeakHandler<H>> handlers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final ExecutorService handlersExecutor;

    /**
     * CAN OVERRIDE THIS METHOD. If you need to add some weirdo filters to events right before handler will be submitted
     * to executor.
     */
    protected void submitHandler(H h, EventWrapper<E, H> ew) {
        handlersExecutor.submit(() -> runHandlerWrapper(h, ew));
    }

    /**
     * CAN OVERRIDE THIS METHOD. This executes in separate thread, passing event to handler
     */
    protected void runHandler(H h, E e) throws Throwable {
        h.handleEvent(e);
    }

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
        Thread eventQueueThread = new Thread(this::eventsQueue, "EventQueue handlers thread");
        eventQueueThread.setDaemon(true);
        eventQueueThread.start();
    }

    @Override
    public void subscribe(H subscriber) {
        Class<? extends EventBusEvent> cls = subscriber.getLinkedClass();
        if (cls == null) {
            handlers.add(new WeakHandler<>(subscriber, gcQueue));
        } else {
            synchronized (this) {
                Set<WeakHandler<H>> hs = handlersCls.computeIfAbsent(cls, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
                hs.add(new WeakHandler<>(subscriber, gcQueue));
            }
        }
    }

    @Override
    public void unsubscribe(H subscriber) {
        Class<?> cls = subscriber.getLinkedClass();
        if (cls == null) {
            handlers.remove(new WeakHandler<>(subscriber, gcQueue));
        } else {
            Set<WeakHandler<H>> set = handlersCls.get(cls);
            if (set != null) {
                set.remove(new WeakHandler<>(subscriber, gcQueue));
            }
        }
    }

    @Override
    public void publish(E event) {
        publish(event, null, null);
    }

    @Override
    public void publish(E event, BiConsumer<E, H> success, FailureConsumer<E, H> failure) {
        if (event == null) {
            return;
        }
        eventsQueue.add(new EventWrapper<>(event, success, failure));
        synchronized (eventsQueue) {
            eventsQueue.notifyAll();
        }
    }

    @Override
    public boolean hasPendingEvents() {
        return !eventsQueue.isEmpty();
    }

    private void eventsQueue() {
        while (true) {
            WeakHandler<?> wh;
            while ((wh = (WeakHandler<?>) gcQueue.poll()) != null) {
                Class<?> cls = wh.getHandlerTypeClass();
                if (cls == null) {
                    handlers.remove(wh);
                } else {
                    Set<WeakHandler<H>> set = handlersCls.get(cls);
                    if (set != null) {
                        set.remove(wh);
                    }
                }
            }

            if (eventsQueue.isEmpty()) {
                synchronized (eventsQueue) {
                    try {
                        eventsQueue.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            EventWrapper<E, H> ew = eventsQueue.poll();
            if (ew != null && ew.event != null) {
                notifySubscribers(ew);
            }
        }
    }

    private void notifySubscribers(EventWrapper<E, H> ew) {
        try {
            Set<WeakHandler<H>> hcls = handlersCls.get(ew.event.getClass());
            if (hcls != null) {
                for (WeakHandler<H> wh : hcls) {
                    H eh = wh.get();
                    if (eh != null) {
                        submitHandler(eh, ew);
                    }
                }
            }

            for (WeakHandler<H> wh : handlers) {
                H eh = wh.get();
                if (eh != null && eh.canHandle(ew.event.getClass())) {
                    submitHandler(eh, ew);
                }
            }
        } catch (Throwable th) {
            logger.error("Event processing fail {}. {}", ew.event.getClass().getSimpleName(), th.getMessage(), th);
        }
    }

    private void runHandlerWrapper(H handler, EventWrapper<E, H> ew) {
        try {
            runHandler(handler, ew.event);
            if (ew.success != null) {
                ew.success.accept(ew.event, handler);
            }
        } catch (Throwable th) {
            logger.error("Handler {} fail on event {}. {}", handler.getClass().getSimpleName(), ew.event.getClass().getSimpleName(), th.getMessage(), th);
            if (ew.failure != null) {
                ew.failure.accept(ew.event, handler, th);
            }
        }
    }
}
