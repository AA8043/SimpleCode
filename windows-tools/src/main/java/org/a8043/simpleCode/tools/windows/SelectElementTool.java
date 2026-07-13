package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class SelectElementTool implements CallableTool {
    public static final Tool TOOL = new Tool("select_element", ToolVisibility.NORMAL_MODE_ONLY,
        new SelectElementTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("element_id", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (!Native.selectElement(args.getStr("element_id"))) {
            throw new ToolException("Failed to select element");
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("element_id");
    }
}
