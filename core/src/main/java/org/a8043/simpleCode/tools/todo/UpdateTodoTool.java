package org.a8043.simpleCode.tools.todo;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.Todo;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolException;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.Arrays;
import java.util.List;

public class UpdateTodoTool implements CallableTool {
    public static final Tool TOOL = new Tool("update_todo", new UpdateTodoTool(), List.of(
        new StringParameter("id", true),
        new StringParameter("newStatus", true,
            Arrays.stream(Todo.Status.values()).map(Enum::name).toList())
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        Todo todo = runningTool.getSession().getTodoList().stream()
            .filter(t -> t.getId().equals(args.getStr("id")))
            .findFirst().orElseThrow(() -> new ToolException("Nonexistent todo: " + args.getStr("id")));
        todo.setStatus(args.getEnum(Todo.Status.class, "newStatus"));
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
