package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class RecordUserPreferenceTool implements CallableTool {
    public static final Tool TOOL = new Tool("record_user_preference",
        new RecordUserPreferenceTool(), NeedConsent.unneed(), List.of(
        new StringParameter("item", true),
        new StringParameter("preference", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        if (SimpleCode.USER_PREFERENCE_JSON.containsKey(args.getStr("item"))) {
            throw new ToolException(TOOL.getPromptJson().getStr("alreadyExists"));
        }
        SimpleCode.USER_PREFERENCE_JSON.set(args.getStr("item"), args.getStr("preference"));
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("item") + ": " + args.getStr("preference");
    }
}
