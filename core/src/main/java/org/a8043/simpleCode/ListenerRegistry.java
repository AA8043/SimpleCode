package org.a8043.simpleCode;

import lombok.Value;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.UserChoice;
import org.a8043.simpleCode.session.content.Content;
import org.a8043.simpleCode.session.tool.RunningTool;

import java.util.ArrayList;
import java.util.List;

public class ListenerRegistry {
    private static final List<ListenerInfo> LIST = new ArrayList<>();

    public static void register(Session session, Listener listener) {
        LIST.add(new ListenerInfo(session, listener));
    }

    public static Listener getListener(Session session) {
        return LIST.stream().filter(info -> info.session == session)
            .findFirst().map(info -> info.listener).orElse(null);
    }

    @Value
    private static class ListenerInfo {
        Session session;
        Listener listener;
    }

    public interface Listener {
        void onComplete(Content content);

        void onFinish();

        void onUserChoice(UserChoice<?> userChoice);

        void onToolCall(RunningTool runningTool);
    }
}
