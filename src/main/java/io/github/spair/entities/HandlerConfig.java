package io.github.spair.entities;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class HandlerConfig {

    private String requestAgentName = "Agent name";
    private String timeZone = "Europe/Moscow";
    private GitHubConfig gitHubConfig = new GitHubConfig();
    private ChangelogConfig changelogConfig = new ChangelogConfig();

    @Data
    public class GitHubConfig {
        private String organizationName = "Org Name";
        private String repositoryName = "Repo Name";
        private String token = "12345";
        private String secretKey = "12345";
    }

    @Data
    public class ChangelogConfig {
        private String pathToChangelog = "/path/to/changelog.html";
        private Html html = new Html();

        @Data
        public class Html {
            /**
             * Used in linked to PR changelogs.
             *
             * Example string: {@code <li class='map'>Changes.<a href='link'>- more -</a></li> }
             */
            private String moreText = "more";

            /**
             * Added after author name.
             *
             * Example string: {@code <h4 class='author'>Author updated:</h4> }
             */
            private String updateText = "updated";

            /**
             * Set of html classes available for changelog. Used in PR markdown.
             */
            private Set<String> availableClasses = new HashSet<>();
        }
    }
}
