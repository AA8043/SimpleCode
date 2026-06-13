package org.a8043.simpleCode.session.content;

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
}
