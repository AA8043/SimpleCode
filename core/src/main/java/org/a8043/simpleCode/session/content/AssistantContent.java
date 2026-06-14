package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.ToString;
import org.a8043.simpleCode.session.Role;
import org.a8043.simpleCode.session.tool.ToolCall;

import java.util.List;

@Getter
@ToString
public class AssistantContent extends Content {
    private final String text;
    private final List<ToolCall> toolCallList;

    public AssistantContent(long time, String text, List<ToolCall> toolCallList) {
        super(time);
        this.text = text;
        this.toolCallList = toolCallList;
    }

    @Override
    public Role getRole() {
        return Role.ASSISTANT;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("type", "assistant").set("time", getTime())
            .set("text", text).set("toolCalls", toolCallList);
    }
}
