package org.a8043.simpleCode;

import cn.hutool.core.io.resource.ResourceUtil;
import org.a8043.simpleCode.api.Api;
import org.a8043.simpleCode.api.OpenAIApi;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolVisibility;
import org.a8043.simpleCode.tools.*;
import org.a8043.simpleCode.tools.planMode.EnterPlanModeTool;
import org.a8043.simpleCode.tools.planMode.ExitPlanModeTool;
import org.a8043.simpleCode.tools.planMode.UpdatePlanTool;
import org.a8043.simpleCode.tools.subAgent.CreateSubAgentTool;
import org.a8043.simpleCode.tools.subAgent.ListSubAgentsTool;
import org.a8043.simpleCode.tools.subAgent.SubAgentTool;
import org.a8043.simpleCode.tools.subAgent.WaitSubAgentTool;
import org.a8043.simpleCode.tools.todo.AddTodoTool;
import org.a8043.simpleCode.tools.todo.ListTodoTool;
import org.a8043.simpleCode.tools.todo.UpdateTodoTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registry {
    public static final Map<String, Api> API_MAP = new HashMap<>();
    public static final List<Tool> TOOL_LIST = new ArrayList<>();
    public static final List<Runnable> AFTER_INIT_LIST = new ArrayList<>();
    public static final Map<String, String> SYSTEM_PROMPT_MAP = new HashMap<>();
    public static final Map<String, String> SUB_AGENT_MAP = new HashMap<>();

    static {
        registerApi("OpenAI", new OpenAIApi());

        registerSystemPrompt("normal", ResourceUtil.readUtf8Str("systemPrompts/normal.md"));
        registerSystemPrompt("normal_sub", ResourceUtil.readUtf8Str("systemPrompts/normal-sub.md"));
        registerSystemPrompt("explore_sub", ResourceUtil.readUtf8Str("systemPrompts/explore-sub.md"));

        SUB_AGENT_MAP.put("normal", "normal_sub");
        SUB_AGENT_MAP.put("explore", "explore_sub");

        registerTool(AskUserTool.TOOL);
        registerTool(ReasoningEffortTool.TOOL);
        registerTool(RunCommandTool.TOOL);

        registerTool(WriteFileTool.TOOL);
        registerTool(ReadFileTool.TOOL);
        registerTool(ListFilesTool.TOOL);
        registerTool(SearchFileTool.TOOL);

        registerTool(ListTodoTool.TOOL);
        registerTool(AddTodoTool.TOOL);
        registerTool(UpdateTodoTool.TOOL);

        registerTool(CreateSubAgentTool.TOOL);
        registerTool(SubAgentTool.TOOL);
        registerTool(ListSubAgentsTool.TOOL);
        registerTool(WaitSubAgentTool.TOOL);

        registerTool(EnterPlanModeTool.TOOL);
        registerTool(UpdatePlanTool.TOOL);
        registerTool(ExitPlanModeTool.TOOL);
    }

    public static List<Tool> getToolList(Session session) {
        List<Tool> list = new ArrayList<>(TOOL_LIST);
        if (session.isPlanMode()) {
            list.removeIf(t -> t.getVisibility() == ToolVisibility.NORMAL_MODE_ONLY);
        } else {
            list.removeIf(t -> t.getVisibility() == ToolVisibility.PLAN_MODE_ONLY);
        }
        return list;
    }

    public static void registerApi(String name, Api api) {
        API_MAP.put(name, api);
    }

    public static void registerTool(Tool tool) {
        TOOL_LIST.add(tool);
    }

    public static void registerAfterInit(Runnable afterInit) {
        AFTER_INIT_LIST.add(afterInit);
    }

    public static Tool getTool(String name) {
        return TOOL_LIST.stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
    }

    public static void registerSystemPrompt(String key, String content) {
        SYSTEM_PROMPT_MAP.put(key, content);
    }
}
