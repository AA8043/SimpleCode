package org.a8043.simpleCode.session.tool;

import lombok.Value;
import org.a8043.simpleCode.session.Status;

@Value
public class ToolCallReturn {
    Status status;
    String content;
}
