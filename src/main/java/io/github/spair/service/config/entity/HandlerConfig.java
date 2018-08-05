package io.github.spair.service.config.entity;

import io.github.spair.service.ByondFiles;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ToString
@Getter
@Setter
@SuppressWarnings("WeakerAccess")
public class HandlerConfig {

    private String requestAgentName = "Agent name";
    private String timeZone = "Europe/Moscow";
    private String imageUploadCode = "1234567890";
    private String handlerUrl = "http://127.0.0.1:8080";
    private GitHubConfig gitHubConfig = new GitHubConfig();
    private ChangelogConfig changelogConfig = new ChangelogConfig();
    private Labels labels = new Labels();
    private DmmBotConfig dmmBotConfig = new DmmBotConfig();

    public boolean validDmePath(final String path) {
        return path.startsWith("/") && path.endsWith(ByondFiles.DME_SUFFIX);
    }

    @ToString
    @Getter
    @Setter
    public class GitHubConfig {
        private String organizationName = "Org Name";
        private String repositoryName = "Repo Name";
        private String token = "12345";
        private String secretKey = "12345";
        private Set<String> masterUsers = new HashSet<>();
    }

    @ToString
    @Getter
    @Setter
    public class Labels {
        private String invalidChangelog = "Invalid Changelog";
        private String mapChanges = "Map Edit";
        private String iconChanges = "Sprites";
        private String workInProgress = "Work In Progress";
        private String doNotMerge = "DO NOT MERGE";
        private String testMerge = "Test Merge";
        private String interactiveDiffMap = "Interactive Diff Map";
        private Map<String, String> labelsForClasses = new HashMap<>();
    }

    @ToString
    @Getter
    @Setter
    public class ChangelogConfig {
        private String pathToChangelog = "/path/to/changelog.html";
        private Html html = new Html();

        @ToString
        @Getter
        @Setter
        public class Html {
            private Set<String> availableClasses = new HashSet<>();
        }
    }

    @ToString
    @Getter
    @Setter
    public class DmmBotConfig {
        private String pathToDme = "/taucetistation.dme";
    }
}
