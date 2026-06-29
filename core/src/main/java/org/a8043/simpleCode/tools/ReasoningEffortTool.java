package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.ReasoningEffort;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.Arrays;
import java.util.List;

public class ReasoningEffortTool implements CallableTool {
    public static final Tool TOOL = new Tool("reasoning_effort", new ReasoningEffortTool(), List.of(
        new StringParameter("new", true,
            Arrays.stream(ReasoningEffort.values()).map(Enum::name).toList())
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        runningTool.getSession().setReasoningEffort(args.getEnum(ReasoningEffort.class, "new"));
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("new");
    }
}
