package org.a8043.simpleCode.session.content;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.ToString;
import org.a8043.simpleCode.session.Role;

import java.util.List;

@Getter
@ToString
public class AssistantContent extends Content {
    private final String text;
    private final List<String> toolCallIdList;

    public AssistantContent(long time, String text, List<String> toolCallIdList) {
        super(time);
        this.text = text;
        this.toolCallIdList = toolCallIdList;
    }

    @Override
    public Role getRole() {
        return Role.ASSISTANT;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("type", "assistant").set("time", getTime())
            .set("text", text).set("toolCalls", toolCallIdList);
    }
}
