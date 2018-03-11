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

@Component
class ChangelogValidator {

    private final ConfigService configService;

    private static final String CODE_QUOTE = "`";

    @Autowired
    ChangelogValidator(final ConfigService configService) {
        this.configService = configService;
    }

    ChangelogValidationStatus validate(final Changelog changelog) {
        ChangelogValidationStatus status = new ChangelogValidationStatus();
        List<String> invalidClasses = parseInvalidClasses(changelog.getChangelogRows());

        if (invalidClasses.size() > 0) {
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
        StringBuilder sb = new StringBuilder();

        sb.append("Reason: unknown classes detected. Next should be changed or removed: ");

        for (int i = 0; i < invalidClasses.size() - 1; i++) {
            sb.append(CODE_QUOTE).append(invalidClasses.get(i)).append(CODE_QUOTE).append(", ");
        }

        sb.append(CODE_QUOTE).append(invalidClasses.get(invalidClasses.size() - 1)).append(CODE_QUOTE).append('.');

        return sb.toString();
    }
}
