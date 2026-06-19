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
        new StringParameter("list_files", "dir", true),
        new StringParameter("list_files", "type", true, List.of("list", "search")),
        new BooleanParameter("list_files", "recursive", true)
    ));

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws ToolException {
        String dir = args.getStr("dir");
        String type = args.getStr("type");
        boolean recursive = args.getBool("recursive");

        File baseDir = new File(dir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new ToolException("Directory does not exist: " + dir);
        }

        List<String> results = new ArrayList<>();

        if ("list".equals(type)) {
            listFiles(baseDir, recursive, results);
        } else if ("search".equals(type)) {
            searchFiles(baseDir, recursive, results);
        } else {
            throw new ToolException("Invalid type: " + type + ". Must be 'list' or 'search'");
        }

        if (results.isEmpty()) {
            return "No files found.";
        }

        return String.join("\n", results);
    }

    private void listFiles(File dir, boolean recursive, List<String> results) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            String prefix = file.isDirectory() ? "[DIR] " : "      ";
            results.add(prefix + file.getAbsolutePath());

            if (recursive && file.isDirectory()) {
                listFiles(file, recursive, results);
            }
        }
    }

    private void searchFiles(File dir, boolean recursive, List<String> results) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                results.add("[FILE] " + file.getAbsolutePath());
            } else if (file.isDirectory()) {
                results.add("[DIR]  " + file.getAbsolutePath());
                if (recursive) {
                    searchFiles(file, recursive, results);
                }
            }
        }
    }
}
