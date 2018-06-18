package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.changelog.entities.ChangelogRow;
import io.github.spair.service.changelog.entities.ChangelogValidationStatus;
import io.github.spair.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class ChangelogValidator {

    private final ConfigService configService;

    private static final String CODE_QUOTE = "`";

    ChangelogValidator(final ConfigService configService) {
        this.configService = configService;
    }

    ChangelogValidationStatus validate(final Changelog changelog) {
        ChangelogValidationStatus status = new ChangelogValidationStatus();
        List<String> invalidClasses = parseInvalidClasses(changelog.getChangelogRows());

        if (!invalidClasses.isEmpty()) {
            status.setMessage(getUnknownClassesReason(invalidClasses));
        } else if (changelog.isEmpty()) {
            status.setMessage("Reason: empty changelog. Please, check markdown correctness.");
        }

        if (status.getMessage() != null) {
            status.setStatus(ChangelogValidationStatus.Status.INVALID);
        }

        return status;
    }

    private List<String> parseInvalidClasses(final List<ChangelogRow> changelogRow) {
        Set<String> availableClasses = configService.getConfig().getChangelogConfig().getHtml().getAvailableClasses();
        List<String> invalidClasses = new ArrayList<>();

        changelogRow.forEach(row -> {
            if (!availableClasses.contains(row.getClassName())) {
                invalidClasses.add(row.getClassName());
            }
        });

        return invalidClasses;
    }

    private String getUnknownClassesReason(final List<String> invalidClasses) {
        return "Reason: unknown classes detected. Next should be changed or removed: ".concat(
                invalidClasses.stream().map(invalidClass -> CODE_QUOTE.concat(invalidClass).concat(CODE_QUOTE))
                        .collect(Collectors.joining(", ")).concat(".")
        );
    }
}
