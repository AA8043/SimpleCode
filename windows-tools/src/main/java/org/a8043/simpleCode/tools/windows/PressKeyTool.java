package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class PressKeyTool implements CallableTool {
    public static final Tool TOOL = new Tool("press_key", ToolVisibility.NORMAL_MODE_ONLY,
        new PressKeyTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("key_combination", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (!Native.pressKey(args.getStr("key_combination"))) {
            throw new ToolException("Failed to press key combination");
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("key_combination");
    }
}
