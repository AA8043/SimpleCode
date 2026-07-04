package org.a8043.simpleCode.cli.commands;

import org.a8043.simpleCode.session.Session;

import java.util.List;

public class ClsCommand implements Command {
    @Override
    public void run(Session session, List<String> argList) {
        session.getAllContentList().clear();
    }
}
