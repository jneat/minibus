# Lightweight event bus for java application

This is easiest way to integrate event processing into your application.
Suits well for single instance application.
Helps you to avoid adding 3rd party message brokers like RabbitMQ, ActiveMQ, HornetQ, etc.

It is very simple event bus implementation, based on observable pattern.
You can use it as dependency or modify it for your needs, it is just several hundreds lines of code.

The **library is stable**. I just don't bump version to 1.0.0 but I guess it is almost there.

[![Release](https://jitpack.io/v/javaplugs/minibus.svg)](https://jitpack.io/#javaplugs/minibus)  
\[[API javadoc](https://jitpack.io/com/github/javaplugs/minibus/-SNAPSHOT/javadoc/)\]

# Some code examples?

You can find code examples in the project tests. See [EventBusTest](./src/test/java/com/github/jneat/minibus/EventBusTest.java).

## Installation
You can add this artifact to your project using [JitPack](https://jitpack.io/#javaplugs/minibus).  
All versions list, instructions for gradle, maven, ivy etc. can be found by link above.

To get latest commit use `master-SNAPSHOT` instead version number.

This library using slf4j-api which should not output messages by default. 
You have to configure proper logger for slf4j in your project to view this messages.

## Define your events model

You should define at least one event DTO that implements EventBusEvent interface.
Also you can create your own Event interface that extends EventBusEvent and use it for your app events.

```java
public class Event1 implements EventBusEvent {
    // with any properties you like
};

public class Event2 implements EventBusEvent {
    // with any properties you like
};

public class Event3 implements EventBusEvent {
    // with any properties you like
};
```
The main point here is that you can have different events with different properties.

## Implement handlers for your events
Any handler should implement EventBusHandler interface.

### Basic handler
Suitable for most cases. 

Basic handlers stored in hash map using event class as key.
Thus handlers quantity should not affect choosing handler performance.

```java
public class Handler1 extends EventBusHandler<Event1> {
    @Override
    protected void handle(Event1 event) {
        // do something with event1
    }
}

public class Handler2 extends EventBusHandler<Event2> {
    @Override
    protected void handle(Event2 event) {
        // do something with event2
    }
}
```

### Advanced handler
Should use than you need to process different events in one place.

Note that advanced handlers selecting approach can not be optimized.
It is always O(N) where N is total number of handlers.

```java
public class HandlerAdvanced extends EventBusHandler<EventBusEvent> {

    // FIRST you must override this method and return null
    @Override
    protected Class<EventBusEvent> getLinkedClass() {
        return null;
    }

    // SECOND override canHandle method
    // Should return true if you interested in processing particular event type
    @Override
    public boolean canHandle(Class<? extends EventBusEvent> cls) {
        // Always return true -> will handle any EventBusEvent
        return true;
    }

    @Override
    protected void handle(EventBusEvent event) {
        // do something with event1, event2 and event3
    }
}
```

## Initializing EventBus & subscribe handlers

Two types of EventBus available:

* EventBusAsync - run handlers in separate thread (suitable for most cases)
* EventBusSimple - run handlers in current thread (good for tests)


**Main thing to remember** - event handlers subscribed using weak links.
You must have normal links to handlers in application if you do not want them to be unsubscribed.
So you should have a collection with your handlers, that available for all app runtime.
In spring application you may not worry about it if your handlers are singleton beans.

**Spring (or other DI framework) example**

```java
@Named
public class EventBusBean {

    private final EventBus<EventBusEvent, EventBusHandler<?>> bus;

    // DI framework should inject all available beans here as list
    @Inject
    EventBusBean(List<EventBusHandler> handlers) {
        this.bus = new EventBusAsync<>();
        handlers.stream().forEach(bus::subscribe);
        // No need to store link to handlers, because DI framework should treat them as singletons
    }

    // Just return event bus
    public EventBus bus() {
        return this.bus;
    }
}
```

## Publishing events

All you need is to create event with valid event type and publish it to EventBus.

```java
public class SomewhereInYourCode {
    // Considering that such variable exists:
    EventBusBean ebb;

    public void something() {
        // Publishing events
        ebb.bus().publish(new Event1());
        ebb.bus().publish(new Event2());
    }
}
```

## Publishing events with callback

Also you can add callbacks on success or failure event processing.

Because there can be many handlers to one event we pass handler to callback.
You should receive as many success/failure calls as number of handlers subscribed to your event.

```java
public class SomewhereInYourCode {
    // Considering that such variable exists:
    EventBusBean ebb;

    public void something() {
        ebb.bus().publish(
            new Event3(),
            (event, handler) -> { 
                // Will be called on success processing for each handler
            },
            (event, handler, excpetion) -> { 
                // Will be called on processing error for each handler
            }
        );
    }
}
```
