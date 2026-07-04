package org.a8043.simpleCode.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.Util;
import org.a8043.simpleCode.session.tool.*;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.io.File;
import java.util.List;

public class WriteFileTool implements CallableTool {
    public static final Tool TOOL = new Tool("write_file", ToolVisibility.NORMAL_MODE_ONLY,
        new WriteFileTool(), List.of(
        new StringParameter("file", true),
        new StringParameter("type", true, List.of("overwrite", "replace", "append")),
        new StringParameter("content", true),
        new StringParameter("target", false)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        File file = new File(args.getStr("file"));
        String oldContent = "";
        if (file.exists()) {
            oldContent = Util.readFile(new File(args.getStr("file")));
        }

        String newContent = switch (args.getStr("type")) {
            case "overwrite" -> args.getStr("content");
            case "replace" -> {
                if (args.getStr("target") == null) {
                    throw new ToolException("Target string is required for replace type");
                }
                yield oldContent.replace(args.getStr("target"), args.getStr("content"));
            }
            case "append" -> oldContent + args.getStr("content");
            default -> throw new ToolException("Invalid write type: " + args.getStr("type"));
        };

        if (runningTool != null && !FileUtil.isSub(runningTool.getSession().getFolder().getDir(), file)) {
            throw new ToolException(TOOL.getPromptJson().getStr("notInWorkspace"));
        }
        Util.writeFile(newContent, file);
        return "";
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return new File(args.getStr("file")).getAbsolutePath();
    }
}
