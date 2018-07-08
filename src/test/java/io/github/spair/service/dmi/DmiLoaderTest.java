package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.byond.dmi.DmiSlurper;
import io.github.spair.service.RestService;
import io.github.spair.service.github.GitHubService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DmiSlurper.class)
public class DmiLoaderTest {

    @Mock
    private RestService restService;
    @Mock
    private GitHubService gitHubService;

    private DmiLoader loader;

    private static final String DMI_NAME = "dmi_name";
    private static final String BASE64 = "base64image";
    private static final String FILE_NAME = "file_name";

    @Before
    public void setUp() {
        loader = new DmiLoader(restService, gitHubService);
        PowerMockito.mockStatic(DmiSlurper.class);
        when(DmiSlurper.slurpUp(anyString(), anyString())).thenReturn(mock(Dmi.class));
    }

    @Test
    public void testLoadFromGitHub() throws Exception {
        when(gitHubService.readEncodedFile(anyString())).thenReturn(BASE64);
        CompletableFuture<Optional<Dmi>> future = loader.loadFromGitHub(DMI_NAME, FILE_NAME);
        assertTrue(future.get().isPresent());
    }

    @Test
    public void testLoadFromGitHubWithException() throws Exception {
        HttpClientErrorException e = getClientHttpException();
        when(gitHubService.readEncodedFile(anyString())).thenThrow(e);
        CompletableFuture<Optional<Dmi>> future = loader.loadFromGitHub(DMI_NAME, FILE_NAME);
        assertFalse(future.get().isPresent());
    }

    @Test
    public void testLoadFromUrl() throws Exception {
        when(restService.getForObject(anyString(), eq(byte[].class))).thenReturn(new byte[0]);
        CompletableFuture<Optional<Dmi>> future = loader.loadFromUrl(DMI_NAME, FILE_NAME);
        assertTrue(future.get().isPresent());
    }

    @Test
    public void testLoadFromUrlWithException() throws Exception {
        HttpClientErrorException e = getClientHttpException();
        when(restService.getForObject(anyString(), eq(byte[].class))).thenThrow(e);
        CompletableFuture<Optional<Dmi>> future = loader.loadFromUrl(DMI_NAME, FILE_NAME);
        assertFalse(future.get().isPresent());
    }

    private HttpClientErrorException getClientHttpException() {
        HttpClientErrorException e = mock(HttpClientErrorException.class, Answers.RETURNS_DEEP_STUBS);
        when(e.getStatusCode().is4xxClientError()).thenReturn(true);
        return e;
    }
}