package io.github.spair.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.config.entity.HandlerConfigStatus;
import io.github.spair.service.github.GitHubService;
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
    public void testValidateConfigWhenAllValid() {
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
    public void testValidateConfigWhenOnlyGitHubValid() {
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
    public void testValidateConfigWhenAllInvalid() {
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