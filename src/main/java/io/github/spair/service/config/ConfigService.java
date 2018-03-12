package io.github.spair.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spair.service.config.entities.HandlerConfig;
import io.github.spair.service.config.entities.HandlerConfigStatus;
import io.github.spair.service.git.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Service
public class ConfigService {

    public static final String CONFIG_NAME = "GWHConfig.json";

    private HandlerConfig configuration;

    private final ObjectMapper objectMapper;
    private final GitHubService gitHubService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    public ConfigService(final ObjectMapper objectMapper, @Lazy final GitHubService gitHubService) {
        this.objectMapper = objectMapper;
        this.gitHubService = gitHubService;
    }

    @PostConstruct
    private void initConfigFile() throws IOException {
        File file = new File(CONFIG_NAME);

        if (file.exists()) {
            configuration = objectMapper.readValue(file, HandlerConfig.class);
            LOGGER.info("Configuration loaded from file");
        } else if (file.createNewFile()) {
            configuration = new HandlerConfig();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, configuration);
            LOGGER.info("Configuration created and loaded. File path: " + file.getAbsolutePath());
        } else {
            throw new IOException("Configuration file creation error");
        }
    }

    public File exportConfig() {
        return new File(CONFIG_NAME);
    }

    @SuppressWarnings("checkstyle:HiddenField")
    public void importConfig(final HandlerConfig configuration) throws IOException {
        this.configuration = configuration;
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_NAME), configuration);
        LOGGER.info("New configuration saved");
    }

    public HandlerConfigStatus validateConfig(final HandlerConfig config) {
        String orgName = config.getGitHubConfig().getOrganizationName();
        String repoName = config.getGitHubConfig().getRepositoryName();
        String changelogPath = config.getChangelogConfig().getPathToChangelog();

        boolean isGitHubOk = gitHubService.isOrgAndRepoExist(orgName, repoName);
        boolean isChangelogOk = gitHubService.isFilePathExist(orgName, repoName, changelogPath);

        boolean isAllOk = isGitHubOk && isChangelogOk;

        return new HandlerConfigStatus(isAllOk, isGitHubOk, isChangelogOk);
    }

    public HandlerConfig getConfig() {
        return configuration;
    }
}
