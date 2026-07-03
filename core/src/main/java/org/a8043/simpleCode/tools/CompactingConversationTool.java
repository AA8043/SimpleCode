package org.a8043.simpleCode.tools;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.api.CompleteResult;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.Status;
import org.a8043.simpleCode.session.content.*;
import org.a8043.simpleCode.session.tool.*;

import java.util.List;

@Slf4j
public class CompactingConversationTool implements CallableTool {
    public static final Tool TOOL = new Tool("compacting_conversation",
        ToolVisibility.ALL, new CompactingConversationTool(), List.of());

    @Override
    public String call(JSONObject args, RunningTool runningTool) throws Exception {
        Session session = runningTool.getSession();
        List<Content> contentList = session.getContentList();
        ToolContent toolContent = new ToolContent(System.currentTimeMillis(), runningTool.getToolCall().getId(),
            Status.success(), "Working");
        contentList.add(toolContent);
        contentList.add(new RemindContent(System.currentTimeMillis(),
            Registry.SYSTEM_PROMPT_MAP.get("compacting-conversation")));
        Model mainModel = Settings.INSTANCE.getMainModel();
        CompleteResult result = mainModel.getProvider().getApi().complete(mainModel, session);

        if (result.getContentList().isEmpty()) {
            throw new ToolException("");
        }
        contentList.removeIf(c -> !(c instanceof SystemContent) &&
                                  !(c instanceof AssistantContent ac &&
                                    ac.getToolCallIdList().contains(runningTool.getToolCall().getId())));
        contentList.remove(toolContent);
        return result.getContentList().getFirst().getText();
    }

    @Override
    public String getSimpleInfo(JSONObject args) {
        return "";
    }
}
