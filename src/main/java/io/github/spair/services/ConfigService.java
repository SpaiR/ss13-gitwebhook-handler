package io.github.spair.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spair.entities.HandlerConfig;
import io.github.spair.entities.HandlerConfigStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Service
public class ConfigService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private GitHubService gitHubService;

    public static final String CONFIG_NAME = "GWHConfig.json";

    private HandlerConfig configuration;
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    @PostConstruct
    public void initConfigFile() throws IOException {
        File file = new File(CONFIG_NAME);

        if (file.exists()) {
            configuration = objectMapper.readValue(file, HandlerConfig.class);
            logger.info("Configuration loaded from file");
        } else if (file.createNewFile()) {
            configuration = new HandlerConfig();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, configuration);
            logger.info("Configuration created and loaded. File path: " + file.getAbsolutePath());
        } else {
            throw new IOException("Configuration file creation error");
        }
    }

    public File downloadConfigFile() {
        return new File(CONFIG_NAME);
    }

    public void saveNewConfig(HandlerConfig configuration) throws IOException {
        this.configuration = configuration;
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_NAME), configuration);
        logger.info("New configuration saved. Config object: " + configuration);
    }

    public HandlerConfigStatus validateConfig(HandlerConfig config) {
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

    public HandlerConfig.ChangelogConfig getChangelogConfig() {
        return configuration.getChangelogConfig();
    }

    public HandlerConfig.GitHubConfig getGitHubConfig() {
        return configuration.getGitHubConfig();
    }
}
