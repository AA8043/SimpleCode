package org.a8043.simpleCode.session;

import org.a8043.simpleCode.Folder;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.util.event.Event;
import org.junit.Test;

import java.io.File;

public class SessionTest {
    @Test
    public void testAsk() {
        SimpleCode.init();
        Session session = new Folder(new File(".")).createSession();
        ask(session, "在这个目录创建一个名为\"hello.txt\"的文件");
    }

    public static void ask(Session session, String question) {
        new Thread(() -> {
            while (true) {
                Event<Object> event = session.getEventQueue().get();
                if (event == null) {
                    break;
                }
                System.out.println("Event: " + event.getData());
                if (event.getData() instanceof UserChoice<?> userChoice) {
                    userChoice.setChoice(true);
                }
                session.getEventQueue().complete(event);
            }
        }).start();
        session.ask(question);
    }
}