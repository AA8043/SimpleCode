package org.a8043.simpleCode.session;

import lombok.Getter;

@Getter
public class Asking {
    private int promptTokens;
    private int cachedTokens;
    private int completionTokens;

    void addPromptTokens(int i) {
        promptTokens += i;
    }

    void addCachedTokens(int i) {
        cachedTokens += i;
    }

    void addCompletionTokens(int i) {
        completionTokens += i;
    }
}
