package org.a8043.simpleCode.util.event;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class Event<T> {
    private final String id;
    private final T data;
    @Setter
    private volatile boolean completed = false;

    public Event(T data) {
        id = UUID.randomUUID().toString();
        this.data = data;
    }
}
