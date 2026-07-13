package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class InputTextTool implements CallableTool {
    public static final Tool TOOL = new Tool("input_text", ToolVisibility.NORMAL_MODE_ONLY,
        new InputTextTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("input_id", true),
        new StringParameter("text", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (!Native.inputText(args.getStr("input_id"), args.getStr("text"))) {
            throw new ToolException("Failed to input text");
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("input_id") + " -> " + args.getStr("text");
    }
}
