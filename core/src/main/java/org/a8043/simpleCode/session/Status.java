package org.a8043.simpleCode.session;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Status extends JSONSupport {
    private final boolean isSuccess;
    private final String failedReason;

    public static Status success() {
        return new Status(true, null);
    }

    public static Status fail(String reason) {
        return new Status(false, reason);
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("isSuccess", isSuccess).set("failedReason", failedReason);
    }
}
