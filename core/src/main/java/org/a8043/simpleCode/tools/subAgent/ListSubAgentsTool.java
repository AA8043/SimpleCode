package org.a8043.simpleCode.tools.subAgent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;

import java.util.List;

public class ListSubAgentsTool implements CallableTool {
    public static final Tool TOOL = new Tool("list_sub_agents", new ListSubAgentsTool(), List.of());

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        return StrUtil.join("\n", runningTool.getSession().getSubList().stream()
            .map(s -> s.getId() + (s.getAsking() != null ? " [Working]" : "")).toList());
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
