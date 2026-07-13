package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;

import java.util.List;

public class GetAllWindowsTool implements CallableTool {
    public static final Tool TOOL = new Tool("get_all_windows", ToolVisibility.NORMAL_MODE_ONLY,
        new GetAllWindowsTool(), NeedConsent.unneed(), List.of());

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws RuntimeException {
        return Native.getAllWindows().toStringPretty();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
