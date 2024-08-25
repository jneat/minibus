package com.github.jneat.minibus;

import java.util.concurrent.atomic.AtomicInteger;

public class Handler3 extends EventBusHandler<Event3> {

    AtomicInteger counter = new AtomicInteger();

    @Override
    public void handle(Event3 event) {
        counter.incrementAndGet();
    }

}
