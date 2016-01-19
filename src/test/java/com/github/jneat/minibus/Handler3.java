package com.github.jneat.minibus;

import com.github.jneat.minibus.EventBusHandler;
import java.util.concurrent.atomic.AtomicInteger;

public class Handler3 extends EventBusHandler<Event3> {

    AtomicInteger counter = new AtomicInteger();

    @Override
    public void handle(Event3 event) {
        counter.incrementAndGet();
    }

}
