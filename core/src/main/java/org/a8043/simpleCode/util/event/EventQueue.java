package org.a8043.simpleCode.util.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class EventQueue<T> {
    private final Map<String, Event<T>> pendingEvents = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition eventAvailable = lock.newCondition();
    private final Condition eventCompleted = lock.newCondition();

    public Event<T> add(T data) {
        Event<T> event = new Event<>(data);
        pendingEvents.put(event.getId(), event);

        lock.lock();
        try {
            eventAvailable.signalAll();
        } finally {
            lock.unlock();
        }

        return event;
    }

    public Event<T> get() {
        lock.lock();
        try {
            while (pendingEvents.isEmpty()) {
                try {
                    eventAvailable.await();
                } catch (InterruptedException ignored) {
                    return null;
                }
            }

            return pendingEvents.get(pendingEvents.keySet().iterator().next());
        } finally {
            lock.unlock();
        }
    }

    public void complete(Event<T> event) {
        lock.lock();
        try {
            Event<T> event1 = pendingEvents.remove(event.getId());
            if (event1 != null) {
                event1.setCompleted(true);
                eventCompleted.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void waitComplete(Event<T> event) {
        lock.lock();
        try {
            while (pendingEvents.containsKey(event.getId())) {
                try {
                    eventCompleted.await();
                } catch (InterruptedException ignored) {
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
