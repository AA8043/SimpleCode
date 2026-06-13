package org.a8043.simpleCode.session;

import lombok.Getter;

import java.util.List;

@Getter
public class UserChoice<T> {
    private final Object content;
    private final List<T> optionList;
    private T choice;

    public UserChoice(Object content, List<T> optionList) {
        this.content = content;
        this.optionList = optionList;
    }

    public void setChoice(Object choice) {
        this.choice = (T) choice;
    }

    public enum Type {
        TOOL_CALL_CONFIRMATION
    }
}
