package org.a8043.simpleCode.session;

import org.a8043.simpleCode.Folder;
import org.a8043.simpleCode.ListenerRegistry;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.session.content.AssistantContent;
import org.a8043.simpleCode.session.content.Content;
import org.a8043.simpleCode.session.content.ToolContent;
import org.a8043.simpleCode.session.tool.RunningTool;
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
        ListenerRegistry.register(session, new ListenerRegistry.Listener() {
            @Override
            public void onComplete(Content content) {
                switch (content) {
                    case AssistantContent ac -> System.out.println("Assistant: " + ac.getText());
                    case ToolContent tc -> System.out.println("Tool: " + tc.getText());
                    default -> {
                    }
                }
            }

            @Override
            public void onFinish() {
                System.out.println("==完成==");
            }

            @Override
            public void onUserChoice(UserChoice<?> userChoice) {
                System.out.println("用户选择: " + userChoice.getContent());
                userChoice.setChoice(true);
            }

            @Override
            public void onToolCall(RunningTool runningTool) {
                System.out.println("=====");
                System.out.println("调用工具: " + runningTool.getToolCall().getTool().getName());
                System.out.println("参数: " + runningTool.getToolCall().getArgs());
                System.out.println("=====");
            }
        });
        session.ask(question);
    }
}