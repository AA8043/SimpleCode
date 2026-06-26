package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.Util;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.io.File;
import java.util.List;

public class ReadFileTool implements CallableTool {
    public static final Tool TOOL = new Tool("read_file", new ReadFileTool(), List.of(
        new StringParameter("read_file", "file", true),
        new StringParameter("read_file", "range", false)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        String content = Util.readFile(new File(args.getStr("file")));
        if (args.containsKey("range")) {
            String range = args.getStr("range");
            String[] parts = range.split("-");
            int startLine = Integer.parseInt(parts[0]);
            int endLine = parts.length > 1 ? Integer.parseInt(parts[1]) : Integer.MAX_VALUE;
            String[] lines = content.split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = startLine - 1; i < Math.min(endLine, lines.length); i++) {
                sb.append(lines[i]).append("\n");
            }
            return sb.toString().trim();
        } else {
            return content;
        }
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return new File(args.getStr("file")).getAbsolutePath();
    }
}
