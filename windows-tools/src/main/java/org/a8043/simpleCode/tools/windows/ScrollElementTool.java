package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.NumberParameter;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class ScrollElementTool implements CallableTool {
    public static final Tool TOOL = new Tool("scroll_element", ToolVisibility.NORMAL_MODE_ONLY,
        new ScrollElementTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("element_id", true),
        new NumberParameter("horizontal_amount", true, -1, 1),
        new NumberParameter("vertical_amount", true, -1, 1)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (!Native.scrollElement(args.getStr("element_id"), args.getInt("horizontal_amount"),
            args.getInt("vertical_amount"))) {
            throw new ToolException("Failed to scroll element");
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("element_id") + " (" + args.getInt("horizontal_amount") + ", " +
               args.getInt("vertical_amount") + ")";
    }
}
