package io.github.spair.handler.command;

import io.github.spair.handler.command.changelog.AddTestChangelogCommand;
import io.github.spair.handler.command.changelog.RemoveTestChangelogCommand;
import io.github.spair.handler.command.changelog.UpdateChangelogCommand;
import io.github.spair.handler.command.changelog.ValidateChangelogCommand;
import io.github.spair.handler.command.diff.DeletePullRequestRepoCommand;
import io.github.spair.handler.command.diff.ReportDmiDiffCommand;
import io.github.spair.handler.command.diff.ReportDmmDiffCommand;
import io.github.spair.handler.command.diff.BuildIDMapCommand;
import io.github.spair.handler.command.diff.DeleteIDMapCommand;
import io.github.spair.handler.command.label.LabelIssueCommand;
import io.github.spair.handler.command.label.LabelPullRequestCommand;
import org.springframework.aop.framework.Advised;

public enum Command {

    LABEL_ISSUE(LabelIssueCommand.class),
    VALIDATE_CHANGELOG(ValidateChangelogCommand.class),
    UPDATE_CHANGELOG(UpdateChangelogCommand.class),
    LABEL_PR(LabelPullRequestCommand.class),
    REPORT_DMI_DIFF(ReportDmiDiffCommand.class),
    REPORT_DMM_DIFF(ReportDmmDiffCommand.class),
    DELETE_PULL_REQUEST_REPO(DeletePullRequestRepoCommand.class),
    ADD_TEST_CHANGELOG(AddTestChangelogCommand.class),
    REMOVE_TEST_CHANGELOG(RemoveTestChangelogCommand.class),
    BUILD_IDMAP(BuildIDMapCommand.class),
    DELETE_IDMAP(DeleteIDMapCommand.class);

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
