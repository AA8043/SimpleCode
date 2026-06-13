package org.a8043.simpleCode.session.content;

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
}
