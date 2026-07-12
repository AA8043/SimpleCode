package org.a8043.simpleCode.tools.planMode;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnterPlanModeTool implements CallableTool {
    static final Map<String, File> PLAN_MAP = new HashMap<>();
    public static final Tool TOOL = new Tool("enter_plan_mode",
        ToolVisibility.NORMAL_MODE_ONLY, new EnterPlanModeTool(), NeedConsent.unneed(), List.of());

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        PLAN_MAP.put(runningTool.getSession().getId(), new File(runningTool.getSession().getFolder().getDataDir(),
            "plans/" + System.currentTimeMillis() + ".md"));
        runningTool.getSession().setPlanMode(true);
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
