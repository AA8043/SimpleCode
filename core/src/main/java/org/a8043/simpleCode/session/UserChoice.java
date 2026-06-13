package org.a8043.simpleCode.session;

import lombok.Getter;

import java.util.List;

@Getter
public class UserChoice<T> {
    private final Object content;
    private final List<T> optionList;
    private final boolean hasCustomization;
    private T choice;

    public UserChoice(Object content, List<T> optionList, boolean hasCustomization) {
        this.content = content;
        this.optionList = optionList;
        this.hasCustomization = hasCustomization;
    }

    public UserChoice(Object content, List<T> optionList) {
        this(content, optionList, false);
    }

    public void setChoice(Object choice) {
        this.choice = (T) choice;
    }
}
