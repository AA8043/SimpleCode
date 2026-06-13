package org.a8043.simpleCode.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.Status;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.ToolCallReturn;

import java.io.File;

public class WriteFileTool implements CallableTool {
    @Override
    public ToolCallReturn call(JSONObject args, RunningTool runningTool) {
        runningTool.setDoing("write");
        runningTool.setContent(args.getStr("file"));
        FileUtil.writeUtf8String(args.getStr("content"), new File(args.getStr("file")));
        return new ToolCallReturn(Status.success(), "");
    }
}
