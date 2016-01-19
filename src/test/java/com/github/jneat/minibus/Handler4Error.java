package com.github.jneat.minibus;

import com.github.jneat.minibus.EventBusHandler;

public class Handler4Error extends EventBusHandler<Event4> {

    @Override
    public void handle(Event4 event) {
        throw new UnsupportedOperationException("This error should be caught");
    }

}
