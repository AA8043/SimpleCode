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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.ListenerRegistry;
import org.a8043.simpleCode.api.ApiException;
import org.a8043.simpleCode.cli.I18n;
import org.a8043.simpleCode.cli.Main;
import org.a8043.simpleCode.cli.Util;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.UserChoice;
import org.a8043.simpleCode.session.content.*;
import org.a8043.simpleCode.session.tool.RunningTool;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.tools.planMode.Plan;

import java.util.ArrayList;
import java.util.List;

import static dev.tamboui.toolkit.Toolkit.*;

@Slf4j
public class SessionView extends Main.View {
    private static final List<SessionView> openedList = new ArrayList<>();
    private Panel unhandledUserChoice;
    private final Object userChoiceLock = new Object();
    private final SpinnerState spinnerState = new SpinnerState();

    public static SessionView open(Session session) {
        return openedList.stream().filter(v -> v.session == session).findFirst().orElseGet(() -> {
            SessionView view = new SessionView(session);
            openedList.add(view);
            return view;
        });
    }

    private final Session session;
    private final ListElement<Object> contentListElement = new ListElement<>()
        .id("contentList").displayOnly().stickyScroll().rounded().fill();

    private SessionView(Session session) {
        this.session = session;
    }

    @Override
    public void init() {
        ListenerRegistry.register(session, new ListenerRegistry.Listener() {
            @Override
            public void onComplete(Content content) {
            }

            @Override
            public void onFinish() {
            }

            @SneakyThrows
            @Override
            public void onUserChoice(UserChoice<?> userChoice) {
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
                    userChoiceLock.wait();
                }
            }

            @Override
            public void onToolCall(RunningTool runningTool) {
            }
        });

        contentListElement.data(session.getAllContentList(),
            content -> column(switch (content) {
                case SystemContent ignored -> text();
                case RemindContent ignored -> text();
                case UserContent uc -> text("> " + uc.getText()).overflow(Overflow.WRAP_WORD);
                case AssistantContent ac -> text("● " + ac.getText()
                    .replace("\n", "    ")).overflow(Overflow.WRAP_WORD);
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
                case ToolCall tc -> row(spinner().state(spinnerState), Util.getToolDescriptionElement(tc));
                case Plan plan -> column(text(I18n.get("session.plan") + "--------"),
                    richTextArea(plan.getPlan()), text("-------------"));
                case ApiException e -> row(text("⚠ ").yellow(), text(I18n.get("session.apiError",
                    String.valueOf(e.getStatus()), e.getContent())).red());
                default -> throw new RuntimeException();
            }, text()));
    }

    private final TextInputState questionInputState = new TextInputState();

    @Override
    public Element render() {
        spinnerState.advance();

        Panel todoPanel = panel().max(20).fill(20).rounded();
        session.getTodoList().forEach(todo -> todoPanel.add(row(switch (todo.getStatus()) {
            case WAITING -> text("● ").blue().overflow(Overflow.WRAP_WORD);
            case DOING -> text("● ").green().overflow(Overflow.WRAP_WORD);
            case FINISHED -> text("● ").gray().overflow(Overflow.WRAP_WORD);
        }, text(todo.getTask()))));

        Panel statisticPanel = panel().max(20).fill(20).rounded();
        if (session.getAsking() != null) {
            statisticPanel.add(
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
                    if (!questionInputState.text().isBlank()) {
                        String question = questionInputState.text();
                        questionInputState.clear();
                        if (session.getAsking() == null) {
                            new Thread(() -> session.ask(question)).start();
                        } else {
                            session.getContentList().add(new UserContent(System.currentTimeMillis(), question));
                        }
                    }
                }).rounded() : unhandledUserChoice
        ).on(KeyTrigger.key(KeyCode.ESCAPE), e -> Main.INSTANCE.setView(MainView.INSTANCE))
            .on(KeyTrigger.ctrl('a'), e -> session.setAutoMode(!session.isAutoMode()));
    }
}
