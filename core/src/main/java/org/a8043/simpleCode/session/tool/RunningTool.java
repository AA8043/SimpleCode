package org.a8043.simpleCode.session.tool;

import lombok.Getter;
import org.a8043.simpleCode.session.Session;

@Getter
public class RunningTool {
    private final ToolCall toolCall;
    private final Session session;

    public RunningTool(ToolCall toolCall, Session session) {
        this.toolCall = toolCall;
        this.session = session;
    }
}
