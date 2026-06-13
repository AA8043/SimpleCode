package org.a8043.simpleCode;

import org.a8043.simpleCode.api.Api;
import org.a8043.simpleCode.api.OpenAIApi;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.tools.AskUserTool;
import org.a8043.simpleCode.tools.ReadFileTool;
import org.a8043.simpleCode.tools.WriteFileTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registry {
    public static final Map<String, Api> API_MAP = new HashMap<>();
    public static final List<Tool> TOOL_LIST = new ArrayList<>();

    static {
        registerApi("OpenAI", new OpenAIApi());
        registerTool(WriteFileTool.TOOL);
        registerTool(ReadFileTool.TOOL);
        registerTool(AskUserTool.TOOL);
    }

    public static void registerApi(String name, Api api) {
        API_MAP.put(name, api);
    }

    public static void registerTool(Tool tool) {
        TOOL_LIST.add(tool);
    }

    public static Tool getTool(String name) {
        return TOOL_LIST.stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
    }
}
