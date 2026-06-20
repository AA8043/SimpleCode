package org.a8043.simpleCode.cli.views;

import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.tui.bindings.KeyTrigger;
import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.widgets.input.TextInputState;
import org.a8043.simpleCode.cli.Main;
import org.a8043.simpleCode.cli.Util;
import org.a8043.simpleCode.session.Session;

import java.util.List;

import static dev.tamboui.toolkit.Toolkit.*;

public class MainView extends Main.View {
    public static final MainView INSTANCE = new MainView();
    private final TextInputState searchInputState = new TextInputState();
    private final ListElement<Session> sessionList = new ListElement<Session>().id("sessionList").autoScroll();

    @Override
    public void init() {
        List<Session> sessionList1 = Main.INSTANCE.getFolder().getSessionList();
        sessionList.on(KeyTrigger.key(KeyCode.ENTER), e ->
            Main.INSTANCE.setView(SessionView.open(sessionList1.get(sessionList.selected()))));
        sessionList.data(sessionList1, Util::getSessionDisplayElement);
    }

    @Override
    public Element render() {
        return panel(
            text("SimpleCode · " + Main.INSTANCE.getFolder().getDir().getAbsolutePath())
                .addClass("main-view-title"),
            textInput(searchInputState).id("searchInput").title("⌕").rounded(),
            sessionList
        ).on(KeyTrigger.ctrl('k'), e -> Main.INSTANCE.getFolder().createSession())
            .on(KeyTrigger.key(KeyCode.ESCAPE), e -> Main.INSTANCE.exit())
            .borderless();
    }
}
