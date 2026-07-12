package org.a8043.simpleCode.tools.subAgent;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.NeedConsent;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.util.List;

public class WaitSubAgentTool implements CallableTool {
    public static final Tool TOOL = new Tool("wait_sub_agent", new WaitSubAgentTool(), NeedConsent.unneed(), List.of(
        new StringParameter("id", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        Session session = runningTool.getSession().getSub(args.getStr("id"));
        while (session.getAsking() != null) {
            ThreadUtil.sleep(100);
        }
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
