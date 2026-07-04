package org.a8043.simpleCode.cli.commands;

import org.a8043.simpleCode.session.Session;

import java.util.List;

public class ContinueCommand implements Command {
    @Override
    public void run(Session session, List<String> argList) {
        new Thread(session::startLoop).start();
    }
}
