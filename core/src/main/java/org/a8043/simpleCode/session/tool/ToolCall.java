package org.a8043.simpleCode.session.tool;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONSupport;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class ToolCall extends JSONSupport {
    Tool tool;
    String id;
    JSONObject args;

    public ToolCallReturn call(RunningTool runningTool) {
        return tool.call(args, runningTool);
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject().set("tool", tool.getName()).set("id", id).set("args", args);
    }
}
