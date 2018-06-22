package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.changelog.entities.ChangelogRow;
import io.github.spair.service.changelog.entities.ChangelogValidationStatus;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.entities.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChangelogService {

    private final HtmlChangelogGenerator htmlChangelogGenerator;
    private final ChangelogValidator changelogValidator;
    private final ChangelogGenerator changelogGenerator;

    @Autowired
    public ChangelogService(final ConfigService configService) {
        this.htmlChangelogGenerator = new HtmlChangelogGenerator(configService);
        this.changelogValidator = new ChangelogValidator(configService);
        this.changelogGenerator = new ChangelogGenerator();
    }

    public Optional<Changelog> createFromPullRequest(final PullRequest pullRequest) {
        return changelogGenerator.generate(pullRequest);
    }

    public ChangelogValidationStatus validateChangelog(final Changelog changelog) {
        return changelogValidator.validate(changelog);
    }

    public String mergeHtmlWithChangelog(final String html, final Changelog changelog) {
        return htmlChangelogGenerator.generate(html, changelog);
    }

    public Set<String> getChangelogClassesList(final PullRequest pullRequest) {
        Optional<Changelog> changelog = changelogGenerator.generate(pullRequest);

        if (changelog.isPresent() && !changelog.get().isEmpty()) {
            return changelog.get().getChangelogRows()
                    .stream().map(ChangelogRow::getClassName).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
