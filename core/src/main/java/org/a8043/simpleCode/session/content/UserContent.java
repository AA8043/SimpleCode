package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.ToString;
import org.a8043.simpleCode.session.Role;

@Getter
@ToString
public class UserContent extends Content {
    private final String text;

    public UserContent(long time, String text) {
        super(time);
        this.text = text;
    }

    @Override
    public Role getRole() {
        return Role.USER;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("type", "user").set("time", getTime()).set("text", text);
    }
}
