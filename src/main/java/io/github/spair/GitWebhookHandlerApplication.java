package io.github.spair;

import io.github.spair.handler.command.Command;
import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableAsync
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class GitWebhookHandlerApplication implements ApplicationRunner {

    @Autowired
    private Set<HandlerCommand<PullRequest>> commands;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        commands.stream().collect(Collectors.toMap(Command::valueOf, Function.identity())).get(Command.REPORT_DMM_DIFF)
                .execute(PullRequest.builder().author("Lexakr").branchName("cargo-remap").number(2571).build());
        System.exit(0);
    }

    public static void main(final String[] args) {
        SpringApplication.run(GitWebhookHandlerApplication.class, args);
    }
}
