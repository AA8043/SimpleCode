package org.a8043.simpleCode.session;

import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.session.content.AssistantContent;
import org.a8043.simpleCode.session.content.ToolContent;
import org.junit.Test;

public class SessionTest {
    @Test
    public void testAsk() {
        Settings.read();
        Session session = new Session();
        session.ask("在这个目录创建一个名为\"hello.txt\"的文件", content -> {
            switch (content) {
                case AssistantContent ac -> System.out.println("Assistant: " + ac.getText());
                case ToolContent tc -> System.out.println("Tool: " + tc.getText());
                default -> {
                }
            }
        }, userChoice -> {
        }, runningTool -> {
            System.out.println("=====");
            System.out.println("调用工具: " + runningTool.getToolCall().getTool().getName());
            System.out.println("参数: " + runningTool.getToolCall().getArgs());
            System.out.println("=====");
            return true;
        }, () -> System.out.println("==完成=="));
    }
}