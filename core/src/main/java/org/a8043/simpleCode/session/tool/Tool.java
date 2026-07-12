package org.a8043.simpleCode.session.tool;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.session.Status;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;
import org.a8043.simpleCode.session.tool.parameter.ToolParameter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Tool {
    private final String name;
    private final JSONObject promptJson;
    private final String description;
    private final ToolVisibility visibility;
    private final CallableTool callableTool;
    private final NeedConsent needConsent;
    private final List<ToolParameter> parameterList;

    public Tool(String name, CallableTool callableTool, NeedConsent needConsent, List<ToolParameter> parameterList) {
        this(name, ToolVisibility.ALL, callableTool, needConsent, parameterList);
    }

    public Tool(String name, ToolVisibility visibility, CallableTool callableTool,
                NeedConsent needConsent, List<ToolParameter> parameterList) {
        this.name = name;
        promptJson = SimpleCode.PROMPT_JSON.getJSONObject("tool").getJSONObject(name);
        this.visibility = visibility;
        this.needConsent = needConsent;
        description = promptJson.getStr("description");
        this.callableTool = callableTool;
        this.parameterList = new ArrayList<>(parameterList);

        if (needConsent.isNeed()) {
            this.parameterList.add(new StringParameter("reason", false));
        }
    }

    public String getParameterDescription(ToolParameter parameter) {
        return !parameter.getName().equals("reason") ? promptJson.getStr(parameter.getName()) :
            SimpleCode.PROMPT_JSON.getStr("reasonDescription");
    }

    public ToolParameter getParameter(String name) {
        return parameterList.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    public ToolCallReturn call(JSONObject args, RunningTool runningTool) {
        try {
            return new ToolCallReturn(Status.success(), callableTool.call(args, runningTool));
        } catch (Exception e) {
            return new ToolCallReturn(Status.fail(e.getMessage()), "");
        }
    }
}
