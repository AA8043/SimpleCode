package org.a8043.simpleCode.cli.views;

import dev.tamboui.style.Overflow;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.elements.Column;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.Panel;
import dev.tamboui.toolkit.elements.TextElement;
import dev.tamboui.tui.bindings.KeyTrigger;
import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.widgets.input.TextInputState;
import dev.tamboui.widgets.spinner.SpinnerState;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.api.ApiException;
import org.a8043.simpleCode.cli.Main;
import org.a8043.simpleCode.cli.Util;
import org.a8043.simpleCode.cli.commands.Command;
import org.a8043.simpleCode.cli.commands.CommandException;
import org.a8043.simpleCode.cli.commands.CommandRegistry;
import org.a8043.simpleCode.frontend.*;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.Todo;
import org.a8043.simpleCode.session.UserChoice;
import org.a8043.simpleCode.session.content.AssistantContent;
import org.a8043.simpleCode.session.content.ImageContent;
import org.a8043.simpleCode.session.content.ToolContent;
import org.a8043.simpleCode.session.content.UserContent;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.tools.WriteFileTool;
import org.a8043.simpleCode.tools.planMode.Plan;
import org.a8043.simpleCode.util.LineChange;
import org.a8043.simpleCode.util.event.Event;
import org.a8043.simpleCode.util.event.EventQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.tamboui.toolkit.Toolkit.*;

@Slf4j
public class SessionView extends Main.View {
    private static final List<SessionView> openedList = new ArrayList<>();
    private final Session session;
    private Panel unhandledUserChoice;
    private final Object userChoiceLock = new Object();
    private final SpinnerState spinnerState = new SpinnerState();
    private final ListElement<Object> contentListElement = new ListElement<>()
        .id("contentList").displayOnly().stickyScroll().rounded().fill();

    public static SessionView open(Session session) {
        return openedList.stream().filter(v -> v.session == session).findFirst().orElseGet(() -> {
            SessionView view = new SessionView(session);
            openedList.add(view);
            return view;
        });
    }

    private SessionView(Session session) {
        this.session = session;
    }

    @Override
    public void init() {
        new Thread(() -> {
            EventQueue<Object> eventQueue = session.getEventQueue();
            Event<Object> event;
            while ((event = eventQueue.get()) != null) {
                switch (event.getData()) {
                    case UserChoice<?> userChoice -> {
                        ListElement<?> listElement = list().focusable().id("optionList");
                        List<?> optionList = userChoice.getOptionList();
                        List<Object> objectList = new ArrayList<>(optionList);
                        if (userChoice.isHasCustomization()) {
                            objectList.add(new TextInputState());
                        }

                        unhandledUserChoice = panel(
                            switch (userChoice.getContent()) {
                                case RunningTool rt -> row(text(I18n.get("session.toolCallRequest") + ": "),
                                    Util.getToolDescriptionElement(rt.getToolCall()));
                                default -> text(userChoice.getContent()).overflow(Overflow.WRAP_WORD);
                            },
                            listElement.data(objectList, o -> {
                                if (o instanceof TextInputState tis) {
                                    return textInput(tis).placeholder(I18n.get("input"))
                                        .id("customizationInput").focusable();
                                } else {
                                    return text(o);
                                }
                            }).on(KeyTrigger.key(KeyCode.ENTER), e -> {
                                Object o = objectList.get(listElement.selected());
                                if (o instanceof TextInputState tis) {
                                    o = tis.text();
                                }
                                userChoice.setChoice(o);
                                unhandledUserChoice = null;
                                synchronized (userChoiceLock) {
                                    userChoiceLock.notifyAll();
                                }
                            })
                        ).addClass("ask-user-panel").rounded();

                        synchronized (userChoiceLock) {
                            try {
                                userChoiceLock.wait();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    case Session.Finish finish -> {
                        if (FrontendSettings.INSTANCE.getMail() != null) {
                            Mail.sendStopWorking(System.currentTimeMillis(), session, finish);
                        }
                    }

                    default -> throw new RuntimeException();
                }
                eventQueue.complete(event);
            }
        }).start();

        contentListElement.data(session.getAllContentList(),
            content -> column(switch (content) {
                case UserContent uc -> text("> " + uc.getText()).overflow(Overflow.WRAP_WORD);
                case AssistantContent ac -> row(text("● "),
                    markupTextArea(new MarkdownToBBCodeRenderer().convert(ac.getText())).overflow(Overflow.WRAP_WORD));
                case ToolContent tc -> {
                    TextElement symbol = text("■ ");
                    if (tc.getStatus().isSuccess()) {
                        symbol.green();
                    } else {
                        symbol.red();
                    }

                    Column column = column(row(symbol, Util.getToolDescriptionElement(tc.getToolCall(session))));
                    if (!tc.getStatus().isSuccess()) {
                        column.add(text("⎿ " + tc.getStatus().getFailedReason())
                            .red().overflow(Overflow.WRAP_WORD));
                    }
                    yield column;
                }
                case ImageContent ignored -> text("■ ", I18n.get("session.image"));

                case RunningTool rt -> column(row(spinner().state(spinnerState),
                        Util.getToolDescriptionElement(rt.getToolCall())),
                    text("⎿ ", I18n.get(switch (rt.getStatus()) {
                        case WAITING -> {
                            if (session.isAutoMode()) {
                                yield "session.toolCall.securityAssessment";
                            } else {
                                yield "session.toolCall.waiting";
                            }
                        }
                        case RUNNING -> "session.toolCall.running";
                        case DONE -> "session.toolCall.done";
                    })));

                case Plan plan -> column(text(I18n.get("session.plan") + "--------"),
                    richTextArea(plan.getPlan()), text("-------------"));

                case WriteFileTool.WritedFile wf -> {
                    Column column = column(text(I18n.get("tools.write_file") + "--------"));
                    wf.getDiff().stream().filter(change -> change.getAction() != LineChange.Action.EQUAL)
                        .forEach(change -> column.add(row(
                            text(switch (change.getAction()) {
                                case INSERT -> change.getNewLineNum();
                                case DELETE -> change.getOldLineNum();
                                default -> throw new RuntimeException();
                            } + " ").gray(),
                            switch (change.getAction()) {
                                case INSERT -> text("+ ").green();
                                case DELETE -> text("- ").red();
                                default -> throw new RuntimeException();
                            },
                            text(change.getContent()).overflow(Overflow.WRAP_WORD)
                        )));
                    yield column;
                }

                case ApiException e -> row(text("⚠ ").yellow(), text(I18n.get("session.apiError",
                    String.valueOf(e.getStatus()), e.getContent())).red());
                case CommandException e -> row(text("⚠ ").yellow(),
                    text(I18n.get("command.error", I18n.get(e.getKey(), e.getArgs().toArray(new String[0])))).red());
                case Exception e -> row(text("⚠ ").yellow(), text(I18n.get("session.error",
                    e.getMessage())).red());

                case Session.Retrying retrying -> row(text("🔄 ").yellow(), text(I18n.get("session.retrying",
                    String.valueOf(retrying.getRetryCount()), String.valueOf(retrying.getMaxRetryCount()),
                    String.valueOf(retrying.getWaitTime()))));

                case Session.Finish finish -> text(I18n.get("session.finish",
                    FrontendUtil.formatDuration(finish.getWorkedTime())) + "========");

                default -> throw new RuntimeException();
            }, text()));
    }

    private final TextInputState questionInputState = new TextInputState();

    @Override
    public Element render() {
        spinnerState.advance();

        ListElement<Todo> todoPanel = list().fill(5).displayOnly().rounded().data(session.getTodoList(),
            todo -> row(switch (todo.getStatus()) {
                case WAITING -> text("● ").blue().overflow(Overflow.WRAP_WORD);
                case DOING -> text("● ").green().overflow(Overflow.WRAP_WORD);
                case FINISHED -> text("● ").gray().overflow(Overflow.WRAP_WORD);
            }, text(todo.getTask())));

        Panel statisticPanel = panel().fill(5).rounded();
        if (session.getAsking() != null) {
            statisticPanel.add(
                text(FrontendUtil.formatDuration(session.getAsking().getWorkedTime())),
                text("↑ " + session.getAsking().getPromptTokens()),
                text("●↑ " + session.getAsking().getCachedTokens()),
                text("↓ " + session.getAsking().getCompletionTokens())
            );
        }

        return column(
            Util.getSessionDisplayElement(session),
            row(
                contentListElement,
                column(todoPanel, statisticPanel)
            ).fill(),
            unhandledUserChoice == null ? textInput().state(questionInputState).id("questionInput")
                .placeholder(I18n.get(session.getAsking() != null ? "session.insertUserMessagesTip" : "session.inputTip"))
                .on(KeyTrigger.key(KeyCode.ENTER), e -> {
                    String text = questionInputState.text();
                    questionInputState.clear();
                    if (!text.isBlank()) {
                        if (text.startsWith("/")) {
                            String[] parts = text.split(" ");
                            String command = parts[0].substring(1);
                            List<String> argList = new ArrayList<>();
                            argList.addAll(Arrays.asList(parts).subList(1, parts.length));
                            Command command1 = CommandRegistry.MAP.get(command);
                            if (command1 != null) {
                                try {
                                    command1.run(session, argList);
                                } catch (CommandException ex) {
                                    session.getAllContentList().add(ex);
                                }
                            } else {
                                session.getAllContentList().add(
                                    new CommandException("command.unknown", List.of(command)));
                            }
                        } else {
                            if (session.getAsking() == null) {
                                new Thread(() -> session.ask(text)).start();
                            } else {
                                session.getContentList().add(new UserContent(System.currentTimeMillis(), text));
                            }
                        }
                    }
                }).rounded() : unhandledUserChoice
        ).on(KeyTrigger.key(KeyCode.ESCAPE), e -> Main.INSTANCE.setView(MainView.INSTANCE))
            .on(KeyTrigger.ctrl('a'), e -> session.setAutoMode(!session.isAutoMode()))
            .on(KeyTrigger.ctrl('w'), e -> session.setForeverMode(!session.isForeverMode()));
    }
}
