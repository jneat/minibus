package io.github.jneat.minibus;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * This handler can handle multiple events by condition
 */
public class Handler234 extends EventBusHandler<EventBusEvent> {

    private static final Logger logger = Logger.getLogger(Handler234.class.getSimpleName());

    AtomicInteger counter = new AtomicInteger();

    @Override
    protected Class<EventBusEvent> getLinkedClass() {
        return null;
    }

    @Override
    public boolean canHandle(Class<? extends EventBusEvent> cls) {
        boolean h = cls.isAssignableFrom(Event2.class)
                || cls.isAssignableFrom(Event3.class)
                || cls.isAssignableFrom(Event4.class);
        logger.info("Result " + h + " for " + cls.getCanonicalName());
        return h;
    }

    @Override
    public void handle(EventBusEvent event) {
        counter.incrementAndGet();
    }
}
