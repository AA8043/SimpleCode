package org.a8043.simpleCode.api;

import lombok.Value;
import org.a8043.simpleCode.session.content.AssistantContent;
import org.a8043.simpleCode.session.tool.ToolCall;

import java.util.List;

@Value
public class CompleteResult {
    boolean isEnd;
    List<AssistantContent> contentList;
    List<ToolCall> toolCallList;
    int promptTokens;
    int cachedTokens;
    int completionTokens;
}
