package io.github.spair.services;

import io.github.spair.services.config.ConfigService;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SignatureServiceTest {

    // SIGN generate from TEXT with '12345' value as secret key
    private final static String SIGN = "1be8f7ad9d858d4883b59bae89cdef3da087ee8e";
    private final static String TEXT = "Some secret text";

    @Test
    public void testValidate() {
        ConfigService configService = mock(ConfigService.class);
        SignatureService signatureService = new SignatureService(configService);

        when(configService.getGitHubSecretKey()).thenReturn("12345");

        signatureService.validate(SIGN, TEXT);
    }

    @Test(expected = InvalidSignatureException.class)
    public void testValidate_WithInvalidSignature() {
        ConfigService configService = mock(ConfigService.class);
        SignatureService signatureService = new SignatureService(configService);

        when(configService.getGitHubSecretKey()).thenReturn("000");

        signatureService.validate(SIGN, TEXT);
    }
}