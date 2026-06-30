package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.session.Role;

public class SystemContent extends Content {
    private final String key;

    public SystemContent(long time, String key) {
        super(time);
        this.key = key;
    }

    @Override
    public String getText() {
        return Registry.SYSTEM_PROMPT_MAP.get(key)
            .replace("{system}", System.getProperty("os.name"))
            .replace("{user_name}", System.getProperty("user.name"))
            .replace("{dir}", System.getProperty("user.dir"));
    }

    @Override
    public Role getRole() {
        return Role.SYSTEM;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("type", "system").set("key", key);
    }
}
