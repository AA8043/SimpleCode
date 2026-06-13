package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.ListenerRegistry;
import org.a8043.simpleCode.session.UserChoice;
import org.a8043.simpleCode.session.tool.*;

import java.util.List;

public class AskUserTool implements CallableTool {
    public static final Tool TOOL = new Tool("ask_user", new ReadFileTool(), List.of(
        new ToolParameter("ask_user", "question", ToolParameter.Type.STRING, true),
        new ToolParameter("ask_user", "options", ToolParameter.Type.ARRAY, true),
        new ToolParameter("ask_user", "hasCustomization", ToolParameter.Type.BOOLEAN, true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws ToolException {
        String question = args.getStr("question");
        List<String> options = args.getJSONArray("options").toList(String.class);
        boolean hasCustomization = args.getBool("hasCustomization");
        UserChoice<String> userChoice = new UserChoice<>(question, options, hasCustomization);
        ListenerRegistry.getListener(runningTool.getSession()).onUserChoice(userChoice);
        String choice = userChoice.getChoice();
        if (choice == null) {
            throw new ToolException("User did not make a choice");
        }
        return choice;
    }
}
