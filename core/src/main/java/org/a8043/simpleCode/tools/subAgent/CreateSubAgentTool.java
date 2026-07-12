package org.a8043.simpleCode.tools.subAgent;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.NeedConsent;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class CreateSubAgentTool implements CallableTool {
    public static final Tool TOOL = new Tool("create_sub_agent", new CreateSubAgentTool(),
        NeedConsent.unneed(), List.of(
        new StringParameter("type", true, Registry.SUB_AGENT_MAP.keySet().stream().toList())
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        return runningTool.getSession().createSub(args.getStr("type")).getId();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("type");
    }
}
