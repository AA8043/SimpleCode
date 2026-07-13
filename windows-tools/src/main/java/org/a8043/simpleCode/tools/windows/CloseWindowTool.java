package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class CloseWindowTool implements CallableTool {
    public static final Tool TOOL = new Tool("close_window", ToolVisibility.NORMAL_MODE_ONLY,
        new CloseWindowTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("window_id", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (!Native.closeWindow(args.getStr("window_id"))) {
            throw new ToolException("Failed to close window");
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("window_id");
    }
}
