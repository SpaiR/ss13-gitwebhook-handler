package io.github.spair.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.config.entity.HandlerConfigStatus;
import io.github.spair.service.github.GitHubService;
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

    public void importConfig(final HandlerConfig config) throws IOException {
        this.configuration = config;
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_NAME), config);
        LOGGER.info("New configuration saved");
    }

    public HandlerConfigStatus validateConfig(final HandlerConfig config) {
        String orgName = config.getGitHubConfig().getOrganizationName();
        String repoName = config.getGitHubConfig().getRepositoryName();
        String changelogPath = config.getChangelogConfig().getPathToChangelog();
        String pathToDme = config.getDmmBotConfig().getPathToDme();

        boolean isGitHubOk = gitHubService.isOrgAndRepoExist(orgName, repoName);
        boolean isChangelogOk = gitHubService.isFilePathExist(orgName, repoName, changelogPath);
        boolean isDmmBotOk = config.validDmePath(pathToDme)
                && gitHubService.isFilePathExist(orgName, repoName, pathToDme);

        boolean isAllOk = isGitHubOk && isChangelogOk && isDmmBotOk;

        return new HandlerConfigStatus(isAllOk, isGitHubOk, isChangelogOk, isDmmBotOk);
    }

    public HandlerConfig getConfig() {
        return configuration;
    }
}
