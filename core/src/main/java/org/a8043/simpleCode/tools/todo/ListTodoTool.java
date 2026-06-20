package org.a8043.simpleCode.tools.todo;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolException;

import java.util.List;

public class ListTodoTool implements CallableTool {
    public static final Tool TOOL = new Tool("list_todo", new ListTodoTool(), List.of());

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws ToolException {
        StringBuilder sb = new StringBuilder();
        runningTool.getSession().getTodoList().forEach(todo -> {
            sb.append("[").append(todo.getId()).append("]");
            sb.append("[").append(todo.getStatus()).append("]");
            sb.append(" ").append(todo.getTask()).append("\n");
        });
        return sb.toString();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
