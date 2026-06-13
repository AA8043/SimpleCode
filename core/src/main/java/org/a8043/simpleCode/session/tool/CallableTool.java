package org.a8043.simpleCode.session.tool;

import cn.hutool.json.JSONObject;

public interface CallableTool {
    ToolCallReturn call(JSONObject args, RunningTool runningTool);
}
