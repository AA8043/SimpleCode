package org.a8043.simpleCode.session;

import cn.hutool.core.annotation.PropIgnore;
import lombok.Getter;
import lombok.Setter;
import org.a8043.simpleCode.ListenerRegistry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.api.CompleteResult;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.content.*;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.session.tool.ToolCallReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Session {
    private final String id;
    @Setter
    private String name;
    private final List<Content> contentList = new ArrayList<>();
    @PropIgnore
    private Asking asking;
    private final List<Todo> todoList = new ArrayList<>();
    @Setter
    private ReasoningEffort reasoningEffort = ReasoningEffort.DEFAULT;

    public Session(String id) {
        this.id = id;
    }

    public static Session create() {
        Session session = new Session(UUID.randomUUID().toString());
        session.getContentList().add(new SystemContent(0));
        return session;
    }

    public void ask(String text) {
        ask(text, Settings.INSTANCE.getCurrentModel());
    }

    public void ask(String text, Model model) {
        ListenerRegistry.Listener listener = ListenerRegistry.getListener(this);
        asking = new Asking();
        contentList.add(new UserContent(System.currentTimeMillis(), text));
        boolean remindedTodo = false;
        while (true) {
            CompleteResult result = model.getProvider().getApi().complete(model, this);
            contentList.addAll(result.getContentList());
            asking.addCompletionTokens(result.getCompletionTokens());
            asking.addCachedTokens(result.getCachedTokens());
            asking.addPromptTokens(result.getPromptTokens());
            result.getContentList().forEach(listener::onComplete);

            List<ToolCall> toolCallList = result.getToolCallList();
            toolCallList.forEach(toolCall -> {
                RunningTool runningTool = new RunningTool(toolCall, this);
                listener.onToolCall(runningTool);
                UserChoice<Boolean> userChoice = new UserChoice<>(runningTool, List.of(true, false));
                listener.onUserChoice(userChoice);
                if (userChoice.getChoice()) {
                    ToolCallReturn callResult = toolCall.call(runningTool);
                    contentList.add(new ToolContent(System.currentTimeMillis(), toolCall,
                        callResult.getStatus(), callResult.getContent()));
                } else {
                    contentList.add(new ToolContent(System.currentTimeMillis(), toolCall,
                        Status.fail("User rejected the tool call"), null));
                }
            });

            if (result.isEnd()) {
                long todoCount = todoList.stream().filter(t -> t.getStatus() != Todo.Status.FINISHED).count();
                if (todoCount == 0 || remindedTodo) {
                    break;
                } else {
                    contentList.add(new RemindContent(System.currentTimeMillis(),
                        SimpleCode.PROMPT_JSON.getStr("hasTodoReminder")));
                    remindedTodo = true;
                }
            }
        }
        listener.onFinish();
        asking = null;
    }
}
