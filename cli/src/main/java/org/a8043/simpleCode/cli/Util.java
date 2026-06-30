package org.a8043.simpleCode.cli;

import dev.tamboui.style.Overflow;
import dev.tamboui.toolkit.elements.Row;
import org.a8043.simpleCode.session.ReasoningEffort;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.tool.ToolCall;

import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.text;

public class Util {
    public static Row getSessionDisplayElement(Session session) {
        Row row = row(text(session.getName() != null ? session.getName() : I18n.get("session.new")));
        if (session.getAsking() != null) {
            row.add(text(" · "), text(I18n.get("session.working")));
        }
        if (session.getReasoningEffort() != ReasoningEffort.DEFAULT) {
            row.add(text(" · "), text(I18n.get("session.reasoningEffort",
                session.getReasoningEffort().name())));
        }
        if (session.isAutoMode()) {
            row.add(text(" · "), text(I18n.get("session.autoMode")));
        }
        if (session.isPlanMode()) {
            row.add(text(" · "), text(I18n.get("session.planMode")));
        }
        if (session.isForeverMode()) {
            row.add(text(" · "), text(I18n.get("session.foreverMode")));
        }
        return row;
    }

    public static Row getToolDescriptionElement(ToolCall toolCall) {
        Row row = row(text(I18n.get("tools." + toolCall.getTool().getName())));
        String simpleInfo = toolCall.getTool().getCallableTool().getSimpleInfo(toolCall.getArgs());
        if (!simpleInfo.isEmpty()) {
            row.add(text("(" + simpleInfo + ")").overflow(Overflow.WRAP_WORD));
        }
        return row;
    }
}
