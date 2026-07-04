package org.a8043.simpleCode.cli.commands;

import cn.hutool.core.io.FileUtil;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.content.UserContent;

import java.io.File;
import java.util.List;

public class FileCommand implements Command {
    @Override
    public void run(Session session, List<String> argList) throws CommandException {
        if (argList.isEmpty()) {
            throw new CommandException("command.argsMissing", List.of());
        }
        File file = new File(argList.getFirst());
        if (!file.exists()) {
            throw new CommandException("command.fileNotFound", List.of(file.getAbsolutePath()));
        }

        String text = FileUtil.readUtf8String(file);
        if (session.getAsking() == null) {
            new Thread(() -> session.ask(text)).start();
        } else {
            session.getContentList().add(new UserContent(System.currentTimeMillis(), text));
        }
    }
}
