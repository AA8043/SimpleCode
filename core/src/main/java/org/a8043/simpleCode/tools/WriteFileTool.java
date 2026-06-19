package org.a8043.simpleCode.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolException;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.io.File;
import java.util.List;

public class WriteFileTool implements CallableTool {
    public static final Tool TOOL = new Tool("write_file", new WriteFileTool(), List.of(
        new StringParameter("write_file", "file", true),
        new StringParameter("write_file", "type", true,
            List.of("overwrite", "replace", "append")),
        new StringParameter("write_file", "content", true),
        new StringParameter("write_file", "target", false)
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
