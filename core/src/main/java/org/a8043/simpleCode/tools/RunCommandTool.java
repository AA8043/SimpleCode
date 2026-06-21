package org.a8043.simpleCode.tools;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolException;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RunCommandTool implements CallableTool {
    public static final Tool TOOL = new Tool("run_command", new RunCommandTool(), List.of(
        new StringParameter("run_command", "command", true
        )));

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
        IoUtil.readUtf8Lines(process.getInputStream(), (LineHandler) line -> sb.append(line).append("\n"));
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new ToolException(e.getMessage());
        }

        return sb.toString();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("command");
    }
}
