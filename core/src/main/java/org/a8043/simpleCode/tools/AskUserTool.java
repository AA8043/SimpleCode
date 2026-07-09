package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.session.UserChoice;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolException;
import org.a8043.simpleCode.session.tool.parameter.ArrayParameter;
import org.a8043.simpleCode.session.tool.parameter.BooleanParameter;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;
import org.a8043.simpleCode.util.event.EventQueue;

import java.util.List;

public class AskUserTool implements CallableTool {
    public static final Tool TOOL = new Tool("ask_user", new AskUserTool(), List.of(
        new StringParameter("question", true),
        new ArrayParameter("options", true, new StringParameter()),
        new BooleanParameter("hasCustomization", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (runningTool.getSession().isAutoMode()) {
            throw new ToolException(SimpleCode.PROMPT_JSON.getStr("autoModeOn"));
        }

        String question = args.getStr("question");
        List<String> options = args.getJSONArray("options").toList(String.class);
        boolean hasCustomization = args.getBool("hasCustomization");
        UserChoice<String> userChoice = new UserChoice<>(question, options, hasCustomization);
        EventQueue<Object> eventQueue = runningTool.getSession().getEventQueue();
        eventQueue.waitComplete(eventQueue.add(userChoice));
        String choice = userChoice.getChoice();
        if (choice == null) {
            throw new ToolException("User did not make a choice");
        }
        return choice;
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
