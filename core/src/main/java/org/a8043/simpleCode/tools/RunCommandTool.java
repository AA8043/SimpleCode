package org.a8043.simpleCode.tools;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.NumberParameter;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RunCommandTool implements CallableTool {
    public static final Tool TOOL = new Tool("run_command", ToolVisibility.NORMAL_MODE_ONLY,
        new RunCommandTool(), List.of(
        new StringParameter("command", true),
        new NumberParameter("timeout", false),
        new StringParameter("reason", false)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        List<String> argList = new ArrayList<>();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            argList.addAll(List.of("cmd.exe", "/c"));
        } else {
            argList.addAll(List.of("sh", "-c"));
        }
        argList.add(args.getStr("command"));

        Process process;
        try {
            process = new ProcessBuilder(argList).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new ToolException(e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        new Thread(() ->
            IoUtil.readUtf8Lines(process.getInputStream(), (LineHandler) line -> sb.append(line).append("\n")))
            .start();

        int timeout = args.getInt("timeout", 5 * 60 * 1000);
        if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
            process.destroy();
            throw new ToolException(TOOL.getPromptJson().getStr("timeout_message"));
        }

        return sb.toString();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("command");
    }
}
