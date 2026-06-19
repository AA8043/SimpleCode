package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.ToString;
import org.a8043.simpleCode.session.Role;
import org.a8043.simpleCode.session.Status;
import org.a8043.simpleCode.session.tool.ToolCall;

@Getter
@ToString
public class ToolContent extends Content {
    private final ToolCall toolCall;
    private final Status status;
    private final String content;

    public ToolContent(long time, ToolCall toolCall, Status status, String content) {
        super(time);
        this.toolCall = toolCall;
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

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("type", "tool").set("time", getTime())
            .set("toolCall", toolCall).set("status", status).set("content", content);
    }
}
