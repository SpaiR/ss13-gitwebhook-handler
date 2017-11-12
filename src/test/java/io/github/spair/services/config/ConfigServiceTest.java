package io.github.spair.services.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spair.services.config.entities.HandlerConfig;
import io.github.spair.services.config.entities.HandlerConfigStatus;
import io.github.spair.services.git.GitHubService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigServiceTest {

    private final static String VAL_ORG = "validOrg";
    private final static String VAL_REP = "validRepo";
    private final static String VAL_PTH = "validPath";

    private final static String INVAL_ORG = "invalidOrg";
    private final static String INVAL_REP = "invalidRepo";
    private final static String INVAL_PTH = "invalidPath";

    private GitHubService gitHubService;

    @Before
    public void setUp() {
        gitHubService = mock(GitHubService.class);

        when(gitHubService.isOrgAndRepoExist(VAL_ORG, VAL_REP)).thenReturn(true);
        when(gitHubService.isFilePathExist(VAL_ORG, VAL_REP, VAL_PTH)).thenReturn(true);

        when(gitHubService.isFilePathExist(VAL_ORG, VAL_REP, INVAL_PTH)).thenReturn(false);

        when(gitHubService.isOrgAndRepoExist(INVAL_ORG, INVAL_REP)).thenReturn(false);
        when(gitHubService.isFilePathExist(INVAL_ORG, INVAL_REP, INVAL_PTH)).thenReturn(false);
    }

    @Test
    public void testValidateConfig_WhenAllValid() {
        ConfigService configService = new ConfigService(mock(ObjectMapper.class), gitHubService);

        HandlerConfig handlerConfig = new HandlerConfig();
        handlerConfig.getGitHubConfig().setOrganizationName(VAL_ORG);
        handlerConfig.getGitHubConfig().setRepositoryName(VAL_REP);
        handlerConfig.getChangelogConfig().setPathToChangelog(VAL_PTH);

        HandlerConfigStatus status = configService.validateConfig(handlerConfig);
        assertTrue(status.allOk);
        assertTrue(status.changelogOk);
        assertTrue(status.gitHubOk);
    }

    @Test
    public void testValidateConfig_WhenOnlyGitHubValid() {
        ConfigService configService = new ConfigService(mock(ObjectMapper.class), gitHubService);

        HandlerConfig handlerConfig = new HandlerConfig();
        handlerConfig.getGitHubConfig().setOrganizationName(VAL_ORG);
        handlerConfig.getGitHubConfig().setRepositoryName(VAL_REP);
        handlerConfig.getChangelogConfig().setPathToChangelog(INVAL_PTH);

        HandlerConfigStatus status = configService.validateConfig(handlerConfig);
        assertFalse(status.allOk);
        assertFalse(status.changelogOk);
        assertTrue(status.gitHubOk);
    }

    @Test
    public void testValidateConfig_WhenAllInvalid() {
        ConfigService configService = new ConfigService(mock(ObjectMapper.class), gitHubService);

        HandlerConfig handlerConfig = new HandlerConfig();
        handlerConfig.getGitHubConfig().setOrganizationName(INVAL_ORG);
        handlerConfig.getGitHubConfig().setRepositoryName(INVAL_REP);
        handlerConfig.getChangelogConfig().setPathToChangelog(INVAL_PTH);

        HandlerConfigStatus status = configService.validateConfig(handlerConfig);
        assertFalse(status.allOk);
        assertFalse(status.changelogOk);
        assertFalse(status.gitHubOk);
    }
}