package org.a8043.simpleCode.cli.commands;

import org.a8043.simpleCode.session.Session;

import java.util.List;

public class RefreshCommand implements Command {
    @Override
    public void run(Session session, List<String> argList) {
        session.getAllContentList().clear();
        session.getAllContentList().addAll(session.getContentList());
    }
}
