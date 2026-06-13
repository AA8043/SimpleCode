package org.a8043.simpleCode.session.content;

import lombok.Getter;
import lombok.ToString;
import org.a8043.simpleCode.session.Role;
import org.a8043.simpleCode.session.Status;

@Getter
@ToString
public class ToolContent extends Content {
    private final String toolCallId;
    private final Status status;
    private final String content;

    public ToolContent(long time, String toolCallId, Status status, String content) {
        super(time);
        this.toolCallId = toolCallId;
        this.status = status;
        this.content = content;
    }

    @Override
    public String getText() {
        return "[" + (status.isSuccess() ? "Success" : "Failed: " + status.getFailedReason()) + "]\n" + content;
    }

    @Override
    public Role getRole() {
        return Role.TOOL;
    }
}
