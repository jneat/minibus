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

package com.github.jneat.minibus.utils;

import com.github.jneat.minibus.EventBus;
import com.github.jneat.minibus.EventBusAsync;
import com.github.jneat.minibus.EventBusEvent;
import com.github.jneat.minibus.EventBusHandler;
import com.github.jneat.minibus.FailureConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EventBroker {

    private static final EventBus<EventBusEvent, EventBusHandler<?>> EVENT_BUS = new EventBusAsync<>();

    private static final EventBroker INSTANCE = new EventBroker();

    private final Map<String, EventBusHandler<?>> registry = new HashMap<>();

    public <T extends EventBusEvent> void subscribe(Consumer<T> consumer, Class<T> type, String handlerName) {
        EventBusHandler<T> handler = new EventBusHandler<T>() {
            private final Class<T> handlerType = type;

            @Override
            protected Class<T> getLinkedClass() {
                return handlerType;
            }

            @Override
            public void handle(T event) {
                consumer.accept(handlerType.cast(event));
            }

            @Override
            public void handleEvent(EventBusEvent event) {
                this.handle(handlerType.cast(event));
            }

            @Override
            public boolean canHandle(Class<? extends EventBusEvent> cls) {
                return handlerType.equals(cls);
            }
        };
        this.subscribe(handler, handlerName);

    }

    public void subscribe(EventBusHandler<?> handler, String handlerName) {

        if (handlerName != null && registry.containsKey(handlerName)) {
            //already subscribed
            return;
        } else {
            registry.put(handlerName, handler);
        }

        EVENT_BUS.subscribe(handler);
    }

    public void unsubscribe(String handlerName) {
        EventBusHandler<?> handler = registry.get(handlerName);
        if (handler != null) {
            EVENT_BUS.unsubscribe(handler);
            registry.remove(handlerName);
        }
    }

    public void publish(EventBusEvent event) {
        EVENT_BUS.publish(event);
    }

    public void publish(EventBusEvent event, BiConsumer<EventBusEvent, EventBusHandler<?>> success,
                        FailureConsumer<EventBusEvent, EventBusHandler<?>> failure) {
        EVENT_BUS.publish(event, success, failure);
    }

    public static EventBroker getInstance() {
        return INSTANCE;
    }
}
