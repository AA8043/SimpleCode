package org.a8043.simpleCode.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.*;

import java.io.File;
import java.util.List;

public class WriteFileTool implements CallableTool {
    public static final Tool TOOL = new Tool("write_file", new WriteFileTool(), List.of(
        new ToolParameter("write_file", "file", ToolParameter.Type.STRING, true),
        new ToolParameter("write_file", "type", ToolParameter.Type.STRING, true,
            List.of("overwrite", "replace", "append")),
        new ToolParameter("write_file", "content", ToolParameter.Type.STRING, true),
        new ToolParameter("write_file", "target", ToolParameter.Type.STRING, false)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws ToolException {
        runningTool.setDoing("write");
        runningTool.setContent(args.getStr("file"));
        File file = new File(args.getStr("file"));
        String oldContent = "";
        if (file.exists()) {
            oldContent = FileUtil.readUtf8String(new File(args.getStr("file")));
        }

        String newContent = switch (args.getStr("type")) {
            case "overwrite" -> args.getStr("content");
            case "replace" -> oldContent.replace(args.getStr("target"), args.getStr("content"));
            case "append" -> oldContent + args.getStr("content");
            default -> throw new ToolException("Invalid write type: " + args.getStr("type"));
        };

        FileUtil.writeUtf8String(newContent, file);
        return "";
    }
}
