package com.github.jneat.minibus;

import java.util.concurrent.atomic.AtomicInteger;

public class Handler1 extends EventBusHandler<Event1> {

    AtomicInteger counter = new AtomicInteger();

    @Override
    public void handle(Event1 event) {
        counter.incrementAndGet();
    }
}
