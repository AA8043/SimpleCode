package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolException;
import org.a8043.simpleCode.session.tool.parameter.BooleanParameter;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListFilesTool implements CallableTool {
    public static final Tool TOOL = new Tool("list_files", new ListFilesTool(), List.of(
        new StringParameter("dir", true),
        new BooleanParameter("recursive", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        String dir = args.getStr("dir");
        boolean recursive = args.getBool("recursive");

        File baseDir = new File(dir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new ToolException("Directory does not exist: " + dir);
        }

        List<String> results = new ArrayList<>();
        listFiles(baseDir, recursive, results);

        if (results.isEmpty()) {
            return "No files found.";
        }

        return String.join("\n", results);
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return new File(args.getStr("dir")).getAbsolutePath();
    }

    private void listFiles(File dir, boolean recursive, List<String> results) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String prefix = file.isDirectory() ? "[Dir] " : "[File] ";
            results.add(prefix + file.getAbsolutePath());

            if (recursive && file.isDirectory()) {
                listFiles(file, true, results);
            }
        }
    }
}
