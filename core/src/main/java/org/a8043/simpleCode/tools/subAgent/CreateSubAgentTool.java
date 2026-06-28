package org.a8043.simpleCode.tools.subAgent;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;

import java.util.List;

public class CreateSubAgentTool implements CallableTool {
    public static final Tool TOOL = new Tool("create_sub_agent", new CreateSubAgentTool(), List.of());

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        return runningTool.getSession().createSub().getId();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
