package com.github.jneat.minibus;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class EventBusAsyncTest {

    Handler1 h1 = new Handler1();

    Handler2 h2 = new Handler2();

    Handler3 h3 = new Handler3();

    Handler4Error h4 = new Handler4Error();

    Handler234 h234 = new Handler234();

    @BeforeClass
    void init() {
    }

    @Test(priority = 10)
    void testAsync() throws InterruptedException {
        EventBusAsync<Event, EventBusHandler<?>> eventBus = new EventBusAsync<>();
        eventBus.subscribe(h1);
        eventBus.subscribe(h2);
        eventBus.subscribe(h3);
        eventBus.subscribe(h234);
        eventBus.subscribe(h4);

        testBus(eventBus);
    }

    @Test(priority = 20)
    void testSimple() throws InterruptedException {
        EventBusSimple<Event, EventBusHandler<?>> eventBus = new EventBusSimple<>();
        eventBus.subscribe(h1);
        eventBus.subscribe(h2);
        eventBus.subscribe(h3);
        eventBus.subscribe(h234);
        eventBus.subscribe(h4);

        testBus(eventBus);
    }

    private void testBus(EventBus<Event, EventBusHandler<?>> eb) throws InterruptedException {
        h1.counter.set(0);
        h2.counter.set(0);
        h3.counter.set(0);
        h234.counter.set(0);

        assertThat(h1.counter).hasValue(0);
        assertThat(h2.counter).hasValue(0);
        assertThat(h3.counter).hasValue(0);
        assertThat(h234.counter).hasValue(0);

        eb.publish(new Event1());
        Thread.sleep(500);

        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(0);
        assertThat(h3.counter).hasValue(0);
        assertThat(h234.counter).hasValue(0);

        eb.publish(new Event2());
        Thread.sleep(500);

        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(1);
        assertThat(h3.counter).hasValue(0);
        assertThat(h234.counter).hasValue(1);

        AtomicInteger e3success = new AtomicInteger(0);
        AtomicInteger e3error = new AtomicInteger(0);
        eb.publish(new Event3(),
            (e, h) -> {
                e3success.incrementAndGet();
            }, (e, h, th) -> {
                e3error.incrementAndGet();
            });
        Thread.sleep(500);

        assertThat(e3success).hasValue(2);
        assertThat(e3error).hasValue(0);
        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(1);
        assertThat(h3.counter).hasValue(1);
        assertThat(h234.counter).hasValue(2);

        AtomicInteger e4success = new AtomicInteger(0);
        AtomicInteger e4error = new AtomicInteger(0);
        eb.publish(new Event4(),
            (e, h) -> {
                e4success.incrementAndGet();
            }, (e, h, th) -> {
                e4error.incrementAndGet();
            });
        Thread.sleep(500);

        assertThat(e4success).hasValue(1);
        assertThat(e4error).hasValue(1);
        assertThat(h1.counter).hasValue(1);
        assertThat(h2.counter).hasValue(1);
        assertThat(h3.counter).hasValue(1);
        assertThat(h234.counter).hasValue(3);
    }
}
