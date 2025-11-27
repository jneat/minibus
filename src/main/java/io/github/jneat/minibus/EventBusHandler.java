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

import java.lang.reflect.ParameterizedType;

/**
 * EventBus event handler.
 * There is no restriction on how many handlers will be subscribed to one or
 * another event type.
 * Keep in mind that handler will be subscribed to EventBus using weak link.
 */
public abstract class EventBusHandler<E extends EventBusEvent> {

    public Class<E> eventClass;

    @SuppressWarnings("unchecked")
    private Class<E> getGenericTypeClass() {
        if (eventClass == null) {
            // noinspection unchecked
            eventClass = (Class<E>) ((ParameterizedType) getClass()
                    .getGenericSuperclass())
                    .getActualTypeArguments()[0];
        }
        return eventClass;
    }

    /**
     * Return exact event type class that must be handled by this handler.
     * Can return null in this case {@link EventBusHandler#canHandle} will be called
     * each time
     * to decide if particular message should be handled.
     *
     * @return Compatible class or null
     */
    protected Class<E> getLinkedClass() {
        return getGenericTypeClass();
    }

    /**
     * If {@link EventBusHandler#getLinkedClass} return null this method will be
     * called to
     * check if current event can be handled here.
     * If getType() result is not null this method will not be called.
     *
     * @param cls Event class
     * @return True if event type can be handled or False.
     */
    public boolean canHandle(Class<? extends EventBusEvent> cls) {
        return false;
    }

    public void handleEvent(EventBusEvent event) throws Throwable {
        this.handle(getGenericTypeClass().cast(event));
    }

    /**
     * This method should handle event of appropriate type.
     */
    public abstract void handle(E event) throws Throwable;
}
