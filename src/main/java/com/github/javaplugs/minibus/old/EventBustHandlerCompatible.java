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

import com.github.jneat.minibus.EventBusHandler;

import java.lang.reflect.ParameterizedType;

/**
 * Created for backward compatibility/migration purpose.
 * This handler can be migrated to {@link EventBusHandler} in your code later.
 */
@Deprecated
public abstract class EventBustHandlerCompatible<E extends Event> implements EventHandler<E> {

    public Class<E> eventClass;

    Class<E> getTypeClass() {
        if (eventClass == null) {
            eventClass = (Class<E>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return eventClass;
    }

    private E toEvent(Event e) {
        if (!e.getClass().isAssignableFrom(getTypeClass())) {
            throw new IllegalArgumentException("Can not cast " + e.getClass().getCanonicalName()
                + " to " + getTypeClass().getCanonicalName());
        }
        return (E)e;
    }

    @Override
    public void handle(Event event) {
        this.handleEvent(toEvent(event));
    }

    /**
     * This method should handle event of appropriate type.
     */
    abstract void handleEvent(E event);
}
