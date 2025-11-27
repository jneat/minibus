package io.github.jneat.minibus;

import java.util.concurrent.atomic.AtomicInteger;

public class Handler2 extends EventBusHandler<Event2> {

    AtomicInteger counter = new AtomicInteger();

    @Override
    public void handle(Event2 event) {
        counter.incrementAndGet();
    }

}
