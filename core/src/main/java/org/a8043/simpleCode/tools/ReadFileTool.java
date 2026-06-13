package org.a8043.simpleCode.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;

import java.util.List;

public class ReadFileTool implements CallableTool {
    public static final Tool TOOL = new Tool("read_file", new ReadFileTool(), List.of(
        new ToolParameter("read_file", "file", ToolParameter.Type.STRING, true),
        new ToolParameter("read_file", "range", ToolParameter.Type.STRING, false)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws ToolException {
        String content;
        try {
            content = FileUtil.readUtf8String(args.getStr("file"));
        } catch (IORuntimeException e) {
            throw new ToolException(e.getMessage());
        }

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
}
