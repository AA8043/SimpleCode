package org.a8043.simpleCode.tools.todo;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.Todo;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.NeedConsent;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;
import java.util.UUID;

public class AddTodoTool implements CallableTool {
    public static final Tool TOOL = new Tool("add_todo", new AddTodoTool(), NeedConsent.unneed(), List.of(
        new StringParameter("task", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        Todo todo = new Todo(args.getStr("task"), UUID.randomUUID().toString().split("-")[0]);
        runningTool.getSession().getTodoList().add(todo);
        return todo.getId();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
