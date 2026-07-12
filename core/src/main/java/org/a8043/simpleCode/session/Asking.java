package org.a8043.simpleCode.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Asking {
    private final long startTime;
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

    public long getWorkedTime() {
        return System.currentTimeMillis() - startTime;
    }
}
