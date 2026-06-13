package org.a8043.simpleCode.session;

import lombok.Getter;
import lombok.Setter;
import org.a8043.simpleCode.ListenerRegistry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.api.CompleteResult;
import org.a8043.simpleCode.session.content.Content;
import org.a8043.simpleCode.session.content.SystemContent;
import org.a8043.simpleCode.session.content.ToolContent;
import org.a8043.simpleCode.session.content.UserContent;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.session.tool.ToolCallReturn;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Session {
    @Setter
    private String name;
    private final List<Content> contentList = new ArrayList<>();

    public static Session create() {
        Session session = new Session();
        session.getContentList().add(new SystemContent(0));
        return session;
    }

    public void ask(String text) {
        ListenerRegistry.Listener listener = ListenerRegistry.getListener(this);
        contentList.add(new UserContent(System.currentTimeMillis(), text));
        while (true) {
            CompleteResult result = Settings.INSTANCE.getCurrentModel().getProvider().getApi()
                .complete(Settings.INSTANCE.getCurrentModel(), contentList);
            contentList.addAll(result.getContentList());
            result.getContentList().forEach(listener::onComplete);

            List<ToolCall> toolCallList = result.getToolCallList();
            toolCallList.forEach(toolCall -> {
                RunningTool runningTool = new RunningTool(toolCall, this);
                listener.onToolCall(runningTool);
                UserChoice<Boolean> userChoice = new UserChoice<>(runningTool, List.of(true, false));
                listener.onUserChoice(userChoice);
                if (userChoice.getChoice()) {
                    ToolCallReturn callResult = toolCall.call(runningTool);
                    contentList.add(new ToolContent(System.currentTimeMillis(), toolCall.getId(),
                        callResult.getStatus(), callResult.getContent()));
                } else {
                    contentList.add(new ToolContent(System.currentTimeMillis(), toolCall.getId(),
                        Status.fail("User rejected the tool call"), null));
                }
            });

            if (result.isEnd()) {
                break;
            }
        }
        listener.onFinish();
    }
}
