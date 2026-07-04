package org.a8043.simpleCode.cli.commands;

import org.a8043.simpleCode.session.Session;

import java.util.List;

public interface Command {
    void run(Session session, List<String> argList) throws CommandException;
}
