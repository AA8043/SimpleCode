package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.session.Role;

public class RemindContent extends Content {
    @Getter
    private final String text;

    public RemindContent(long time, String key) {
        super(time);
        text = SimpleCode.PROMPT_JSON.getStr(key);
    }

    @Override
    public Role getRole() {
        return Role.SYSTEM;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("type", "remind").set("time", getTime()).set("text", text);
    }
}
