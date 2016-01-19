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

import java.lang.reflect.Constructor;

/**
 * Builder for Event type.
 * You can create builder for your own event type only by extending it
 * and implementing your own create method.
 * Or you can implement create method in any part of your code.
 */
@Deprecated
public class EventBuilder<E extends Event> {

    private final E event;

    public EventBuilder(E event) {
        this.event = event;
    }

    /**
     * Create new builder for event with specific type.
     */
    public static EventBuilder create(String eventType) {
        return new EventBuilder(new Event(eventType));
    }

    /**
     * Create new builder for any event subclass with specific type.
     */
    public static <T extends Event> EventBuilder<T> create(Class<T> cls, String eventType) {
        try {
            Constructor<T> constructor = cls.getConstructor(String.class);
            return new EventBuilder<>(constructor.newInstance(eventType));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can not create event builder for " + cls + " and type " + eventType, ex);
        }
    }

    /**
     * Put property into event properties.
     */
    public EventBuilder<E> set(String key, Object value) {
        event.set(key, value);
        return this;
    }

    public E build() {
        return this.event;
    }
}
