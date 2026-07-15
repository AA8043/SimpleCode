package org.a8043.simpleCode.cli.commands;

import cn.hutool.core.io.FileUtil;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.content.ImageContent;
import org.a8043.simpleCode.session.content.UserContent;

import java.io.File;
import java.util.Base64;
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

        byte[] bytes = FileUtil.readBytes(file);
        String suffix = FileUtil.getSuffix(file);
        if (suffix != null && suffix.equals("png")) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            session.getContentList().add(new ImageContent(System.currentTimeMillis(), base64));
        } else {
            session.getContentList().add(new UserContent(System.currentTimeMillis(), new String(bytes)));
        }
    }
}
