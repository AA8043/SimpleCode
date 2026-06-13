package org.a8043.simpleCode.session.tool;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.session.Status;

import java.util.List;

@Getter
public class Tool {
    private final String name;
    private final String description;
    private final CallableTool callableTool;
    private final List<ToolParameter> parameterList;

    public Tool(String name, CallableTool callableTool, List<ToolParameter> parameterList) {
        this.name = name;
        description = Settings.PROMPT_JSON.getByPath("tool." + name + ".description", String.class);
        this.callableTool = callableTool;
        this.parameterList = parameterList;
    }

    public ToolCallReturn call(JSONObject args, RunningTool runningTool) {
        try {
            return new ToolCallReturn(Status.success(), callableTool.call(args, runningTool));
        } catch (ToolException e) {
            return new ToolCallReturn(Status.fail(e.getMessage()), "");
        }
    }
}
