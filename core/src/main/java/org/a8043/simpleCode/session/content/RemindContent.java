package org.a8043.simpleCode.session.content;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import lombok.Getter;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.session.Role;

public class RemindContent extends Content {
    @Getter
    private final String text;

    public RemindContent(long time, String text) {
        super(time);
        this.text = text;
    }

    public static RemindContent ofPromptKey(long time, String key, String... args) {
        return new RemindContent(time, StrUtil.format(SimpleCode.PROMPT_JSON.getStr(key), (Object[]) args));
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
