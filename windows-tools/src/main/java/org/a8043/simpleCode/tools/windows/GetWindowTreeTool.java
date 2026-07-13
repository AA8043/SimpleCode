package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class GetWindowTreeTool implements CallableTool {
    public static final Tool TOOL = new Tool("get_window_tree", ToolVisibility.NORMAL_MODE_ONLY,
        new GetWindowTreeTool(), new NeedConsent(true, List.of()), List.of(
        new StringParameter("window_id", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws RuntimeException {
        return Native.getWindowTree(args.getStr("window_id")).toStringPretty();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("window_id");
    }
}
