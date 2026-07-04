package org.a8043.simpleCode.cli.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CommandException extends Exception {
    private final String key;
    private final List<String> args;
}
