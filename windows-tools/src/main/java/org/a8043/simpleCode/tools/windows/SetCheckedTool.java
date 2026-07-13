package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.BooleanParameter;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class SetCheckedTool implements CallableTool {
    public static final Tool TOOL = new Tool("set_checked", ToolVisibility.NORMAL_MODE_ONLY,
        new SetCheckedTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("element_id", true),
        new BooleanParameter("checked", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (!Native.setChecked(args.getStr("element_id"), args.getBool("checked"))) {
            throw new ToolException("Failed to set checked state");
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("element_id") + " -> " + args.getBool("checked");
    }
}
