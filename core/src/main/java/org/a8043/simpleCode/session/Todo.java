package org.a8043.simpleCode.session;

import lombok.Data;

@Data
public class Todo {
    private final String task;
    private final String id;
    private Status status = Status.WAITING;

    public enum Status {
        FINISHED, DOING, WAITING
    }
}
