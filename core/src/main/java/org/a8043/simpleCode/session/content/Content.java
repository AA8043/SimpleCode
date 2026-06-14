package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONSupport;
import lombok.Getter;
import org.a8043.simpleCode.session.Role;

public abstract class Content extends JSONSupport {
    @Getter
    private final long time;

    protected Content(long time) {
        this.time = time;
    }

    public abstract String getText();

    public abstract Role getRole();

    @Override
    public abstract JSONObject toJSON();
}
