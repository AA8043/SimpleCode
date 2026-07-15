package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import org.a8043.simpleCode.session.Role;

public class ImageContent extends Content {
    @Getter
    private final String base64;

    public ImageContent(long time, String base64) {
        super(time);
        this.base64 = base64;
    }

    @Override
    public String getText() {
        return base64;
    }

    @Override
    public Role getRole() {
        return Role.IMAGE;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("type", "image").set("time", getTime()).set("base64", base64);
    }
}
