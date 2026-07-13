package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class ActivateWindowTool implements CallableTool {
    public static final Tool TOOL = new Tool("activate_window", ToolVisibility.NORMAL_MODE_ONLY,
        new ActivateWindowTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("window_id", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (!Native.activateWindow(args.getStr("window_id"))) {
            throw new ToolException("Failed to activate window");
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("window_id");
    }
}
