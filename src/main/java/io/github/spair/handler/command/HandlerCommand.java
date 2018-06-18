package io.github.spair.handler.command;

public interface HandlerCommand<T> {

    void execute(final T objToProcess);
}
