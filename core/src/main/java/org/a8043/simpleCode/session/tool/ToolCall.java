package org.a8043.simpleCode.session.tool;

import cn.hutool.json.JSONObject;
import lombok.Value;

@Value
public class ToolCall {
    Tool tool;
    String id;
    JSONObject args;

    public ToolCallReturn call(RunningTool runningTool) {
        return tool.call(args, runningTool);
    }
}
