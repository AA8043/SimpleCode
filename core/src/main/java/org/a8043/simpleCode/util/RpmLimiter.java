package org.a8043.simpleCode.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RpmLimiter {
    @Getter
    private final int rpm;
    private long windowStartMillis = System.currentTimeMillis();
    private int usedRequests = 0;

    public synchronized boolean acquire() {
        while (true) {
            long now = System.currentTimeMillis();
            long elapsed = now - windowStartMillis;

            if (elapsed >= 60000) {
                windowStartMillis = now;
                usedRequests = 0;
            }

            if (usedRequests < rpm) {
                usedRequests++;
                return true;
            }

            try {
                wait(60000 - elapsed);
            } catch (InterruptedException e) {
                return false;
            }
        }
    }
}

