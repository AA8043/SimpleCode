package org.a8043.simpleCode.session.content;

import cn.hutool.core.io.resource.ResourceUtil;
import org.a8043.simpleCode.session.Role;

public class SystemContent extends Content {
    public SystemContent(long time) {
        super(time);
    }

    @Override
    public String getText() {
        return ResourceUtil.readUtf8Str("systemPrompt.md")
            .replace("{system}", System.getProperty("os.name"))
            .replace("{user_name}", System.getProperty("user.name"))
            .replace("{dir}", System.getProperty("user.dir"));
    }

    @Override
    public Role getRole() {
        return Role.SYSTEM;
    }
}
