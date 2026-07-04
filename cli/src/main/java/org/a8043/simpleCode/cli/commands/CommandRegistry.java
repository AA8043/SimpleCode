package org.a8043.simpleCode.cli.commands;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    public static final Map<String, Command> MAP = new HashMap<>();

    static {
        register("file", new FileCommand());
        register("continue", new ContinueCommand());
        register("refresh", new RefreshCommand());
        register("cls", new ClsCommand());
    }

    public static void register(String str, Command command) {
        MAP.put(str, command);
    }
}
