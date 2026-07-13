package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class ClickButtonTool implements CallableTool {
    public static final Tool TOOL = new Tool("click_button", ToolVisibility.NORMAL_MODE_ONLY,
        new ClickButtonTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("button_id", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (!Native.clickButton(args.getStr("button_id"))) {
            throw new ToolException("Failed to click button");
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("button_id");
    }
}
