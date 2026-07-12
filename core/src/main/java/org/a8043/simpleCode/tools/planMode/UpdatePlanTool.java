package org.a8043.simpleCode.tools.planMode;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;
import org.a8043.simpleCode.tools.WriteFileTool;

import java.util.List;

public class UpdatePlanTool implements CallableTool {
    public static final Tool TOOL = new Tool("update_plan",
        ToolVisibility.PLAN_MODE_ONLY, new UpdatePlanTool(), NeedConsent.unneed(), List.of(
        new StringParameter("type", true, List.of("overwrite", "replace", "append")),
        new StringParameter("content", true),
        new StringParameter("target", false)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        return WriteFileTool.TOOL.getCallableTool().call(args.clone().set("file",
            EnterPlanModeTool.PLAN_MAP.get(runningTool.getSession().getId()).getAbsolutePath()), null);
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
