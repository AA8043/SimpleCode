package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolException;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SubAgentTool implements CallableTool {
    public static final Tool TOOL = new Tool("sub_agent", new SubAgentTool(), new ArrayList<>(List.of(
        new StringParameter("sub_agent", "task", true),
        new StringParameter("sub_agent", "level", true, new ArrayList<>())
    )));

    static {
        Registry.registerAfterInit(() -> ((StringParameter) TOOL.getParameter("level")).getEnumList().addAll(
            Settings.INSTANCE.getModelList().stream().map(m -> String.valueOf(m.getLevel())).toList()));
    }

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws ToolException {
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

        Session tempSession = new Session(UUID.randomUUID().toString());
        tempSession.ask(args.getStr("task"), model);
        return tempSession.getContentList().getLast().getText();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("task");
    }
}
