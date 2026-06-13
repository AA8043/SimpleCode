package org.a8043.simpleCode.session.tool;

import lombok.Getter;
import lombok.Setter;

@Getter
public class RunningTool {
    private final ToolCall toolCall;
    @Setter
    private String doing;
    @Setter
    private String content;
    @Setter
    private boolean isFinish;

    public RunningTool(ToolCall toolCall) {
        this.toolCall = toolCall;
    }
}
