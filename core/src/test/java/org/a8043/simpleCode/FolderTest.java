package org.a8043.simpleCode;

import org.a8043.simpleCode.session.SessionTest;
import org.junit.Test;

import java.io.File;

public class FolderTest {
    @Test
    public void testCreate() {
        SimpleCode.init();
        Folder folder = new Folder(new File("."));
        SessionTest.ask(folder.createSession(), "你好");
        folder.saveSessions();
    }

    @Test
    public void testRead() {
        SimpleCode.init();
        Folder folder = new Folder(new File("."));
        SessionTest.ask(folder.getSessionList().getFirst(), "创建一个hello.txt文件");
        folder.saveSessions();
    }
}