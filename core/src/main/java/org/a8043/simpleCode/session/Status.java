package org.a8043.simpleCode.session;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Status {
    private final boolean isSuccess;
    private final String failedReason;

    public static Status success() {
        return new Status(true, null);
    }

    public static Status fail(String reason) {
        return new Status(false, reason);
    }
}
