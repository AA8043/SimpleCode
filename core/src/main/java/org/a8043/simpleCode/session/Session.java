package org.a8043.simpleCode.session;

import cn.hutool.core.annotation.PropIgnore;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.Folder;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.api.CompleteResult;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.content.*;
import org.a8043.simpleCode.session.tool.NeedConsent;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.session.tool.ToolCallReturn;
import org.a8043.simpleCode.util.RpmLimiter;
import org.a8043.simpleCode.util.event.Event;
import org.a8043.simpleCode.util.event.EventQueue;

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
            allContentList.add(content);
            return super.add(content);
        }

        @Override
        public boolean addAll(Collection<? extends Content> c) {
            allContentList.addAll(c);
            return super.addAll(c);
        }
    };
    private final List<ToolCall> toolCallList = new ArrayList<>();
    private Asking asking;
    private final List<Todo> todoList = new CopyOnWriteArrayList<>();
    @Setter
    private ReasoningEffort reasoningEffort = ReasoningEffort.DEFAULT;
    private boolean isAutoMode;
    private boolean isPlanMode;
    private boolean isForeverMode;
    @Setter
    private boolean allowTool = true;
    private final List<Session> subList = new ArrayList<>();
    @PropIgnore
    private final Session parent;
    @PropIgnore
    private final List<Object> allContentList = new CopyOnWriteArrayList<>() {
        @Override
        public boolean add(Object o) {
            if (!(o instanceof Content c) || c.getRole() != Role.SYSTEM) {
                return super.add(o);
            } else {
                return false;
            }
        }

        @Override
        public boolean addAll(Collection<?> c) {
            return super.addAll(c.stream().filter(c1 -> !(c1 instanceof Content c2) ||
                                                        c2.getRole() != Role.SYSTEM).toList());
        }
    };
    @PropIgnore
    private final EventQueue<Object> eventQueue = new EventQueue<>();

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
        session.getContentList().add(new SystemContent(0, promptKey, folder.getDir().getAbsolutePath()));
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
        contentList.add(new UserContent(System.currentTimeMillis(), text));
        startLoop(text, model);
    }

    public void startLoop() {
        List<String> userContentList = contentList.stream().filter(c -> c instanceof UserContent)
            .map(Content::getText).toList();
        String last;
        if (!userContentList.isEmpty()) {
            last = userContentList.getLast();
        } else {
            last = "";
        }
        startLoop(last, Settings.INSTANCE.getMainModel());
    }

    public void startLoop(String question, Model model) {
        asking = new Asking(System.currentTimeMillis());
        boolean remindedTodo = false;
        loop:
        while (true) {
            RpmLimiter rpmLimiter = model.getProvider().getRpmLimiter();
            if (Thread.interrupted() || (rpmLimiter != null && !rpmLimiter.acquire())) {
                break;
            }

            CompleteResult result;
            int retryCount = 0;
            while (true) {
                try {
                    result = model.getProvider().getApi().complete(model, this);
                    break;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    allContentList.add(e);
                    if (retryCount++ < 10) {
                        int waitTime = retryCount * 5;
                        allContentList.add(new Retrying(retryCount, 10, waitTime));
                        ThreadUtil.sleep(waitTime * 1000L);
                    } else {
                        break loop;
                    }
                }
            }

            contentList.addAll(result.getContentList());
            asking.addCompletionTokens(result.getCompletionTokens());
            asking.addCachedTokens(result.getCachedTokens());
            asking.addPromptTokens(result.getPromptTokens());
            if (parent != null) {
                result.getContentList().forEach(c -> parent.contentList.add(
                    RemindContent.ofPromptKey(c.getTime(), "subAgentMessage", id, c.getText())));
            }

            List<ToolCall> toolCallList = result.getToolCallList();
            this.toolCallList.addAll(toolCallList);
            toolCallList.forEach(toolCall -> {
                RunningTool runningTool = new RunningTool(toolCall, this);
                allContentList.add(runningTool);
                toolCall.getTool().getCallableTool().beforeRequest(toolCall.getArgs(), runningTool);

                String allow;
                NeedConsent needConsent = toolCall.getTool().getNeedConsent();
                if (needConsent.isNeed()) {
                    if (isAutoMode) {
                        allow = assessmentToolCall(question, toolCall);
                    } else {
                        UserChoice<Boolean> userChoice = new UserChoice<>(runningTool, List.of(true, false));
                        eventQueue.waitComplete(eventQueue.add(userChoice));
                        allow = userChoice.getChoice() ? "" : SimpleCode.PROMPT_JSON.getStr("userRejectedToolCall");
                    }
                } else {
                    allow = "";
                }

                if (allow.isEmpty()) {
                    runningTool.setStatus(RunningTool.Status.RUNNING);
                    ToolCallReturn callResult = toolCall.call(runningTool);
                    contentList.add(new ToolContent(System.currentTimeMillis(), toolCall,
                        callResult.getStatus(), callResult.getContent()));
                } else {
                    contentList.add(new ToolContent(System.currentTimeMillis(), toolCall,
                        Status.fail(allow), ""));
                }
                runningTool.setStatus(RunningTool.Status.DONE);
                allContentList.remove(runningTool);
            });

            if (result.isEnd()) {
                long todoCount = todoList.stream().filter(t -> t.getStatus() != Todo.Status.FINISHED).count();
                if (todoCount == 0 || remindedTodo) {
                    if (!isForeverMode) {
                        break;
                    } else {
                        contentList.add(RemindContent.ofPromptKey(System.currentTimeMillis(), "foreverModeReminder"));
                    }
                } else {
                    contentList.add(RemindContent.ofPromptKey(System.currentTimeMillis(), "hasTodoReminder"));
                    remindedTodo = true;
                }
            }
        }
        Finish finish = new Finish(asking.getWorkedTime());
        eventQueue.waitComplete(eventQueue.add(finish));
        allContentList.add(finish);
        asking = null;
    }

    private String assessmentToolCall(String userMessage, ToolCall toolCall) {
        Session session = Session.create(Type.NORMAL, folder, null,
            "safety-assessment");
        session.setAutoModeDirectly(true);
        session.setAllowTool(false);

        new Thread(() -> {
            Event<Object> event;
            while ((event = session.getEventQueue().get()) != null) {
                session.getEventQueue().complete(event);
            }
        }).start();

        JSONObject args = new JSONObject();
        toolCall.getArgs().forEach((k, v) -> {
            if (!k.equals("reason") && !toolCall.getTool().getNeedConsent().getExcludedParameterList().contains(k)) {
                args.set(k, v);
            }
        });

        session.ask("Task: %s\nTool: %s\nArgs: %s\nReason: %s".formatted(userMessage,
                toolCall.getTool().getName(), args.toString(),
                toolCall.getArgs().getStr("reason", SimpleCode.PROMPT_JSON.getStr("none"))),
            Settings.INSTANCE.getLowestLevelModel());

        Content last = session.getContentList().getLast();
        if (last instanceof AssistantContent ac) {
            return ac.getText().equals("true") ? "" : ac.getText();
        } else {
            return SimpleCode.PROMPT_JSON.getStr("safetyEvaluatorNotAvailable");
        }
    }

    public void setAutoMode(boolean autoMode) {
        if (isAutoMode = autoMode) {
            contentList.add(RemindContent.ofPromptKey(System.currentTimeMillis(), "autoModeOn"));
        } else {
            contentList.add(RemindContent.ofPromptKey(System.currentTimeMillis(), "autoModeOff"));
        }
    }

    public void setAutoModeDirectly(boolean autoMode) {
        isAutoMode = autoMode;
    }

    public void setPlanMode(boolean planMode) {
        if (isPlanMode = planMode) {
            contentList.add(RemindContent.ofPromptKey(System.currentTimeMillis(), "planModeOn"));
        } else {
            contentList.add(RemindContent.ofPromptKey(System.currentTimeMillis(), "planModeOff"));
        }
    }

    public void setPlanModeDirectly(boolean planMode) {
        isPlanMode = planMode;
    }

    public void setForeverMode(boolean foreverMode) {
        if (isForeverMode = foreverMode) {
            contentList.add(RemindContent.ofPromptKey(System.currentTimeMillis(), "foreverModeOn"));
        } else {
            contentList.add(RemindContent.ofPromptKey(System.currentTimeMillis(), "foreverModeOff"));
        }
    }

    public void setForeverModeDirectly(boolean foreverMode) {
        isForeverMode = foreverMode;
    }

    public ToolCall getToolCall(String id) {
        return toolCallList.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public enum Type {
        NORMAL, SUB
    }

    @Value
    public static class Retrying {
        int retryCount;
        int maxRetryCount;
        int waitTime;
    }

    @Value
    public static class Finish {
        long workedTime;
    }
}
