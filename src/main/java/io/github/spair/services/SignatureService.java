package io.github.spair.services;

import io.github.spair.services.exceptions.InvalidSignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

@Service
public class SignatureService {

    private final ConfigService configService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureService.class);
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    @Autowired
    public SignatureService(ConfigService configService) {
        this.configService = configService;
    }

    public void validate(String signature, String data) throws InvalidSignatureException {
        try {
            String realSecretKey = configService.getConfig().getGitHubConfig().getSecretKey();
            SecretKeySpec signingKey = new SecretKeySpec(realSecretKey.getBytes(), HMAC_SHA1_ALGORITHM);

            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            if (!signature.equals(bytesToString(mac.doFinal(data.getBytes())))) {
                throw new InvalidSignatureException();
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            LOGGER.error("Signature validation error", e);
            throw new RuntimeException(e);
        }
    }

    private String bytesToString(byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }

            return formatter.toString();
        }
    }
}
