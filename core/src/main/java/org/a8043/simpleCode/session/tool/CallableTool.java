package org.a8043.simpleCode.session.tool;

import cn.hutool.json.JSONObject;

public interface CallableTool {
    String call(JSONObject args, RunningTool runningTool) throws ToolException;

    String getSimpleInfo(JSONObject args);
}
