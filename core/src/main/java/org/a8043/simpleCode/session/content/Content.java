package org.a8043.simpleCode.session.content;

import lombok.Getter;
import org.a8043.simpleCode.session.Role;

public abstract class Content {
    @Getter
    private final long time;

    protected Content(long time) {
        this.time = time;
    }

    public abstract String getText();

    public abstract Role getRole();
}
