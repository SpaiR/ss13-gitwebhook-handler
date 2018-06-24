package io.github.spair.handler.command;

import org.springframework.aop.framework.Advised;

public enum Command {

    LABEL_ISSUE(LabelIssueCommand.class),
    VALIDATE_CHANGELOG(ValidateChangelogCommand.class),
    UPDATE_CHANGELOG(UpdateChangelogCommand.class),
    LABEL_PR(LabelPullRequestCommand.class),
    REPORT_DMI_DIFF(ReportDmiDiffCommand.class);

    private final Class classOfCommand;

    Command(final Class classOfCommand) {
        this.classOfCommand = classOfCommand;
    }

    public static Command valueOf(final HandlerCommand handlerCommand) {
        for (Command command : values()) {
            if (((Advised) handlerCommand).getTargetSource().getTargetClass().equals(command.classOfCommand)) {
                return command;
            }
        }
        throw new IllegalArgumentException();
    }
}
