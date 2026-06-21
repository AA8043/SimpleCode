package org.a8043.simpleCode.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.session.tool.CallableTool;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.Tool;
import org.a8043.simpleCode.session.tool.ToolException;
import org.a8043.simpleCode.session.tool.parameter.BooleanParameter;
import org.a8043.simpleCode.session.tool.parameter.StringParameter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SearchFileTool implements CallableTool {
    public static final Tool TOOL = new Tool("search_file", new SearchFileTool(), List.of(
        new StringParameter("search_file", "dir", true),
        new StringParameter("search_file", "type", true, List.of("name", "content")),
        new BooleanParameter("search_file", "keyword", true)
    ));
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws ToolException {
        Pattern pattern = Pattern.compile(args.getStr("keyword"));
        File file = new File(args.getStr("dir"));
        if (!file.exists()) {
            throw new ToolException(SimpleCode.PROMPT_JSON.getJSONObject("tool")
                .getJSONObject("search_file").getStr("dirNotExist"));
        }

        List<String> results = switch (args.getStr("type")) {
            case "name" -> searchByName(file, pattern);
            case "content" -> searchByContent(file, pattern);
            default -> throw new ToolException("Invalid type: " + args.getStr("type"));
        };
        if (!results.isEmpty()) {
            return StrUtil.join("\n", results);
        } else {
            return SimpleCode.PROMPT_JSON.getJSONObject("tool")
                .getJSONObject("search_file").getStr("notFound");
        }
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return args.getStr("keyword");
    }

    private List<String> searchByName(File dir, Pattern keyword) throws ToolException {
        List<String> results = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(dir.toPath())) {
            for (Path path : (Iterable<Path>) stream::iterator) {
                String fileName = path.getFileName().toString();
                if (keyword.matcher(fileName).find()) {
                    String absolutePath = path.toAbsolutePath().toString();
                    results.add("[" + absolutePath + "]");
                }
            }
        } catch (Exception e) {
            throw new ToolException(e.getMessage());
        }
        return results;
    }

    private List<String> searchByContent(File dir, Pattern keyword) throws ToolException {
        List<String> results = new ArrayList<>();

        try (var stream = Files.walk(dir.toPath())) {
            for (Path path : (Iterable<Path>) stream::iterator) {
                if (Files.isRegularFile(path)) {
                    long size = Files.size(path);
                    if (size > MAX_FILE_SIZE) {
                        continue;
                    }

                    List<String> lines;
                    try {
                        lines = FileUtil.readUtf8Lines(path.toFile());
                    } catch (IORuntimeException e) {
                        continue;
                    }
                    boolean hasMatch = false;

                    for (int i = 0; i < lines.size(); i++) {
                        if (keyword.matcher(lines.get(i)).find()) {
                            if (!hasMatch) {
                                hasMatch = true;
                            }
                            results.add("[" + path.toAbsolutePath() + "] L" + (i + 1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ToolException(e.getMessage());
        }

        return results;
    }
}
