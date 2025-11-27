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
package io.github.jneat.minibus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Simple event bus with no background threads, if you have few handlers or do
 * not want to have background threads. All
 * consumers will be called directly during event publishing. Publishing event
 * handlers complexity is O(N) we just do
 * linear search without optimizations.
 */
public class EventBusSimple<E extends EventBusEvent, H extends EventBusHandler<?>> implements EventBus<E, H> {

    private static final Logger logger = LoggerFactory.getLogger(EventBusSimple.class);

    private final ReferenceQueue<H> gcQueue = new ReferenceQueue<H>();

    private final AtomicInteger processing = new AtomicInteger();

    private final Set<WeakHandler<H>> handlers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * CAN OVERRIDE THIS METHOD. This actually runs handler. You can add some hooks
     * here.
     */
    protected void runHandler(H h, E e) throws Throwable {
        h.handleEvent(e);
    }

    @Override
    public void subscribe(H subscriber) {
        handlers.add(new WeakHandler<>(subscriber, gcQueue));
    }

    @Override
    public void unsubscribe(H subscriber) {
        handlers.remove(new WeakHandler<>(subscriber, gcQueue));
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
        processing.incrementAndGet();
        try {
            processEvent(new EventWrapper<>(event, success, failure));
        } finally {
            processing.decrementAndGet();
        }
    }

    @Override
    public boolean hasPendingEvents() {
        return processing.get() > 0;
    }

    @SuppressWarnings("unchecked")
    private void processEvent(EventWrapper<E, H> ew) {
        WeakHandler<H> wh;
        // noinspection unchecked
        while ((wh = (WeakHandler<H>) gcQueue.poll()) != null) {
            handlers.remove(wh);
        }
        if (ew != null) {
            notifySubscribers(ew);
        }
    }

    private void notifySubscribers(EventWrapper<E, H> ew) {
        for (WeakHandler<H> wh : handlers) {
            H eh = wh.get();
            if (eh == null) {
                continue;
            }

            try {
                if (eh.getLinkedClass() == null) {
                    if (eh.canHandle(ew.event.getClass())) {
                        runHandlerWrapper(eh, ew);
                    }
                } else if (eh.getLinkedClass().equals(ew.event.getClass())) {
                    runHandlerWrapper(eh, ew);
                }
            } catch (Throwable th) {
                logger.error("Event processing fail {}. {}", ew.event.getClass().getSimpleName(), th.getMessage(), th);
            }
        }
    }

    private void runHandlerWrapper(H eh, EventWrapper<E, H> ew) {
        try {
            runHandler(eh, ew.getEvent());
            if (ew.success != null) {
                ew.success.accept(ew.event, eh);
            }
        } catch (Throwable ex) {
            logger.error("Handler processing fail for {}. {}", ew.event.getClass().getSimpleName(), ex.getMessage(),
                    ex);
            if (ew.failure != null) {
                ew.failure.accept(ew.event, eh, ex);
            }
        }
    }

}
