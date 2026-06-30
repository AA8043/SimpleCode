package org.a8043.simpleCode.tools.planMode;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolVisibility;

import java.io.File;
import java.util.List;

public class ExitPlanModeTool implements CallableTool {
    public static final Tool TOOL = new Tool("exit_plan_mode",
        ToolVisibility.PLAN_MODE_ONLY, new ExitPlanModeTool(), List.of());

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        EnterPlanModeTool.PLAN_MAP.remove(runningTool.getSession().getId());
        runningTool.getSession().setPlanMode(false);
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }

    @Override
    public void beforeRequest(JSONObject args, RunningTool runningTool) {
        File file = EnterPlanModeTool.PLAN_MAP.get(runningTool.getSession().getId());
        runningTool.getSession().getAllContentList().add(new Plan(FileUtil.readUtf8String(file)));
    }
}
