package org.a8043.simpleCode.tools.subAgent;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SubAgentTool implements CallableTool {
    public static final Tool TOOL = new Tool("sub_agent", new SubAgentTool(), NeedConsent.unneed(), List.of(
        new StringParameter("id", true),
        new StringParameter("task", true),
        new StringParameter("level", true, new ArrayList<>())
    ));

    static {
        Registry.registerAfterInit(() -> ((StringParameter) TOOL.getParameter("level")).getEnumList().addAll(
            Settings.INSTANCE.getModelList().stream().map(m -> String.valueOf(m.getLevel())).toList()));
    }

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        int level = Integer.parseInt(args.getStr("level"));
        Model model = Settings.INSTANCE.getModelList().stream()
            .filter(m -> m.getLevel() == level)
            .findFirst().orElse(null);
        if (model == null) {
            model = Settings.INSTANCE.getModelList().stream()
                .filter(m -> m.getLevel() < level)
                .max(Comparator.comparingInt(Model::getLevel))
                .orElseThrow(() -> new ToolException("No suitable model found for level: " + level));
        }
        Model finalModel = model;

        Session session = runningTool.getSession().getSub(args.getStr("id"));
        new Thread(() -> session.startLoop(args.getStr("task"), finalModel)).start();
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("task");
    }
}
