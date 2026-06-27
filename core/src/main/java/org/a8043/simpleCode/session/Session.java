package org.a8043.simpleCode.session;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.ListenerRegistry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.api.CompleteResult;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.content.*;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.session.tool.ToolCallReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Getter
public class Session {
    private final String id;
    @Setter
    private String name;
    private final List<Content> contentList = new CopyOnWriteArrayList<>();
    private final List<ToolCall> toolCallList = new ArrayList<>();
    private Asking asking;
    private final List<Todo> todoList = new ArrayList<>();
    @Setter
    private ReasoningEffort reasoningEffort = ReasoningEffort.DEFAULT;
    private boolean isAutoMode;

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
            CompleteResult result;
            try {
                result = model.getProvider().getApi().complete(model, this);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                break;
            }

            contentList.addAll(result.getContentList());
            asking.addCompletionTokens(result.getCompletionTokens());
            asking.addCachedTokens(result.getCachedTokens());
            asking.addPromptTokens(result.getPromptTokens());
            result.getContentList().forEach(listener::onComplete);

            List<ToolCall> toolCallList = result.getToolCallList();
            this.toolCallList.addAll(toolCallList);
            toolCallList.forEach(toolCall -> {
                RunningTool runningTool = new RunningTool(toolCall, this);
                listener.onToolCall(runningTool);

                boolean isAllow;
                if (!isAutoMode) {
                    UserChoice<Boolean> userChoice = new UserChoice<>(runningTool, List.of(true, false));
                    listener.onUserChoice(userChoice);
                    isAllow = userChoice.getChoice();
                } else {
                    isAllow = true;
                }

                if (isAllow) {
                    ToolCallReturn callResult = toolCall.call(runningTool);
                    contentList.add(new ToolContent(System.currentTimeMillis(), toolCall,
                        callResult.getStatus(), callResult.getContent()));
                } else {
                    contentList.add(new ToolContent(System.currentTimeMillis(), toolCall,
                        Status.fail("User rejected the tool call"), ""));
                }
            });

            if (result.isEnd()) {
                long todoCount = todoList.stream().filter(t -> t.getStatus() != Todo.Status.FINISHED).count();
                if (todoCount == 0 || remindedTodo) {
                    break;
                } else {
                    contentList.add(new RemindContent(System.currentTimeMillis(), "hasTodoReminder"));
                    remindedTodo = true;
                }
            }
        }
        listener.onFinish();
        asking = null;
    }

    public void setAutoMode(boolean autoMode) {
        if (isAutoMode = autoMode) {
            contentList.add(new RemindContent(System.currentTimeMillis(), "autoModeOn"));
        } else {
            contentList.add(new RemindContent(System.currentTimeMillis(), "autoModeOff"));
        }
    }

    public void setAutoModeDirectly(boolean autoMode) {
        isAutoMode = autoMode;
    }

    public ToolCall getToolCall(String id) {
        return toolCallList.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
}
