package org.a8043.simpleCode.util;

import lombok.Value;

@Value
public class LineChange {
    Action action;
    int oldLineNum;
    int newLineNum;
    String content;

    public enum Action {
        DELETE, INSERT, EQUAL
    }
}
