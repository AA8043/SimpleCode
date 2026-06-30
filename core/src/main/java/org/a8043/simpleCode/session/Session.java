package org.a8043.simpleCode.session;

import cn.hutool.core.annotation.PropIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.Folder;
import org.a8043.simpleCode.ListenerRegistry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.api.CompleteResult;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.content.*;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.session.tool.ToolCallReturn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Getter
@EqualsAndHashCode
public class Session {
    @PropIgnore
    private final Folder folder;
    private final Type type;
    private final String id;
    @Setter
    private String name;
    private final List<Content> contentList = new ArrayList<>() {
        @Override
        public boolean add(Content content) {
            if (content.getRole() != Role.SYSTEM) {
                allContentList.add(content);
            }
            return super.add(content);
        }

        @Override
        public boolean addAll(Collection<? extends Content> c) {
            allContentList.addAll(c.stream().filter(c1 -> c1.getRole() != Role.SYSTEM).toList());
            return super.addAll(c);
        }
    };
    private final List<ToolCall> toolCallList = new ArrayList<>();
    private Asking asking;
    private final List<Todo> todoList = new ArrayList<>();
    @Setter
    private ReasoningEffort reasoningEffort = ReasoningEffort.DEFAULT;
    private boolean isAutoMode;
    private boolean isPlanMode;
    private final List<Session> subList = new ArrayList<>();
    @PropIgnore
    private final Session parent;
    @PropIgnore
    private final List<Object> allContentList = new CopyOnWriteArrayList<>();

    public Session(Folder folder, Type type, String id, Session parent) {
        this.folder = folder;
        this.type = type;
        this.id = id;
        this.parent = parent;
    }

    public static Session create(Type type, Folder folder, Session parent, String promptKey) {
        String uuid = UUID.randomUUID().toString();
        Session session = new Session(folder, type,
            type == Type.NORMAL ? uuid : "sub-" + uuid.split("-")[0], parent);
        session.getContentList().add(new SystemContent(0, promptKey));
        return session;
    }

    public Session createSub(String promptKey) {
        Session session = create(Type.SUB, folder, this, promptKey);
        session.setName("Sub-" + session.getId());
        session.setAutoMode(true);
        subList.add(session);
        return session;
    }

    public Session getSub(String id) {
        return subList.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    public void ask(String text) {
        ask(text, Settings.INSTANCE.getMainModel());
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
                allContentList.add(e);
                break;
            }

            contentList.addAll(result.getContentList());
            asking.addCompletionTokens(result.getCompletionTokens());
            asking.addCachedTokens(result.getCachedTokens());
            asking.addPromptTokens(result.getPromptTokens());
            if (parent != null) {
                result.getContentList().forEach(c -> parent.contentList.add(
                    new RemindContent(c.getTime(), "subAgentMessage", id, c.getText())));
            }
            result.getContentList().forEach(listener::onComplete);

            List<ToolCall> toolCallList = result.getToolCallList();
            this.toolCallList.addAll(toolCallList);
            toolCallList.forEach(toolCall -> {
                RunningTool runningTool = new RunningTool(toolCall, this);
                listener.onToolCall(runningTool);
                allContentList.add(toolCall);
                toolCall.getTool().getCallableTool().beforeRequest(toolCall.getArgs(), runningTool);

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
                allContentList.remove(toolCall);
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

    public void setPlanMode(boolean planMode) {
        if (isPlanMode = planMode) {
            contentList.add(new RemindContent(System.currentTimeMillis(), "planModeOn"));
        } else {
            contentList.add(new RemindContent(System.currentTimeMillis(), "planModeOff"));
        }
    }

    public void setPlanModeDirectly(boolean planMode) {
        isPlanMode = planMode;
    }

    public ToolCall getToolCall(String id) {
        return toolCallList.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public enum Type {
        NORMAL, SUB
    }
}
