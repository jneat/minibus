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

import java.util.function.BiConsumer;

public class EventWrapper<E extends EventBusEvent, H extends EventBusHandler<?>> {

    protected final E event;

    protected final BiConsumer<E, H> success;

    protected final FailureConsumer<E, H> failure;

    public EventWrapper(
        E event,
        BiConsumer<E, H> success,
        FailureConsumer<E, H> failure
    ) {
        this.event = event;
        this.success = success;
        this.failure = failure;
    }

    public E getEvent() {
        return this.event;
    }

}
