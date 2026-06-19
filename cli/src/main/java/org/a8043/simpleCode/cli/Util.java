package org.a8043.simpleCode.cli;

import dev.tamboui.toolkit.elements.Row;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.tools.ReadFileTool;
import org.a8043.simpleCode.tools.WriteFileTool;

import java.io.File;

import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.text;

public class Util {
    public static Row getSessionDisplayElement(Session session) {
        Row row = row(text(session.getName() != null ? session.getName() : I18n.get("session.new")));
        if (session.isWorking()) {
            row.add(text(" · "), text(I18n.get("session.working")));
        }
        return row;
    }

    public static Row getToolDescriptionElement(ToolCall toolCall) {
        String type;
        String info;
        switch (toolCall.getTool().getCallableTool()) {
            case ReadFileTool ignored -> {
                type = "read";
                info = new File(toolCall.getArgs().getStr("file")).getAbsolutePath();
            }
            case WriteFileTool ignored -> {
                type = "write";
                info = new File(toolCall.getArgs().getStr("file")).getAbsolutePath();
            }
            default -> throw new RuntimeException();
        }
        return row(text(I18n.get("tools." + type) + "(" + info + ")"));
    }
}
