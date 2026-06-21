package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.tool.ToolCallReturn;
import org.junit.Test;

public class SearchFileToolTest {
    @Test
    public void testSearchContent() {
        ToolCallReturn result = SearchFileTool.TOOL.call(new JSONObject()
            .set("dir", "..").set("type", "content").set("keyword", "MAX_FILE_SIZE"), null);
        System.out.println(result.getContent());
    }

    @Test
    public void testSearchName() {
        ToolCallReturn result = SearchFileTool.TOOL.call(new JSONObject()
            .set("dir", "..").set("type", "name").set("keyword", "SearchFile"), null);
        System.out.println(result.getContent());
    }
}