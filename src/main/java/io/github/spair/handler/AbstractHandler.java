package io.github.spair.handler;

import io.github.spair.handler.command.Command;
import io.github.spair.handler.command.HandlerCommand;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractHandler<T> implements Handler {

    private Map<Command, HandlerCommand<T>> commands;

    void collectCommands(final Set<HandlerCommand<T>> commandsToFilter) {
        commands = commandsToFilter.stream().collect(Collectors.toMap(Command::valueOf, Function.identity()));
    }

    void executeCommands(final T objToProcess, final Command... commandsToExecute) {
        for (Command command : commandsToExecute) {
            commands.get(command).execute(objToProcess);
        }
    }

    Command[] wrapCommands(final Command... commandsToWrap) {
        return commandsToWrap;
    }
}
