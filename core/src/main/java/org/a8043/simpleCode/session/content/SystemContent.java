package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.session.Role;

public class SystemContent extends Content {
    private final String key;
    private final String workingDir;

    public SystemContent(long time, String key, String workingDir) {
        super(time);
        this.key = key;
        this.workingDir = workingDir;
    }

    @Override
    public String getText() {
        return Registry.SYSTEM_PROMPT_MAP.get(key)
            .replace("{system}", System.getProperty("os.name"))
            .replace("{user_name}", System.getProperty("user.name"))
            .replace("{dir}", workingDir);
    }

    @Override
    public Role getRole() {
        return Role.SYSTEM;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("type", "system").set("key", key).set("workingDir", workingDir);
    }
}
