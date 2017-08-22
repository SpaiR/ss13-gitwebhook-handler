package io.github.spair.services.changelog;

import io.github.spair.entities.Changelog;
import io.github.spair.entities.ChangelogRow;
import io.github.spair.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
class ChangelogValidator {

    @Autowired
    private ConfigService configService;

    ChangelogValidationStatus validate(Changelog changelog) {
        ChangelogValidationStatus status = new ChangelogValidationStatus();
        List<String> invalidClasses = parseInvalidClasses(changelog.getChangelogRows());

        if (invalidClasses.size() > 0) {
            StringBuilder sb = new StringBuilder();

            sb.append("Reason: unknown classes detected. Next should be changed or removed: ");

            for (int i = 0; i < invalidClasses.size() - 1; i++) {
                sb.append("`").append(invalidClasses.get(i)).append("`, ");
            }

            sb.append("`").append(invalidClasses.get(invalidClasses.size() - 1)).append("`.");

            status.setMessage(sb.toString());
        } else if (changelog.getChangelogRows().size() == 0) {
            status.setMessage("Reason: empty changelog. Please, check for markdown correctness.");
        }

        if (status.getMessage() != null) {
            status.setStatus(ChangelogValidationStatus.Status.INVALID);
        }

        return status;
    }

    private List<String> parseInvalidClasses(List<ChangelogRow> changelogRow) {
        Set<String> availableClasses = configService.getChangelogConfig().getHtml().getAvailableClasses();
        List<String> invalidClasses = new ArrayList<>();

        changelogRow.forEach(row -> {
            if (!availableClasses.contains(row.getClassName())) {
                invalidClasses.add(row.getClassName());
            }
        });

        return invalidClasses;
    }
}
