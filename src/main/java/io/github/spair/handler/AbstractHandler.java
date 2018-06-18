package io.github.spair.handler;

import io.github.spair.handler.command.Command;
import io.github.spair.handler.command.HandlerCommand;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractHandler<T> implements Handler {

    Map<Command, HandlerCommand<T>> commands;

    Map<Command, HandlerCommand<T>> filterCommands(final Set<HandlerCommand<T>> commands) {
        return commands.stream().collect(Collectors.toMap(Command::valueOf, Function.identity()));
    }

    void executeCommands(final T objToProcess, final Command... commands) {
        for (Command command : commands) {
            this.commands.get(command).execute(objToProcess);
        }
    }

    Command[] wrapCommands(final Command... commands) {
        return commands;
    }
}
