package io.github.spair.service;

import io.github.spair.service.config.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HandlerUrlService {

    private static final String DEFAULT_HOSTNAME = "http://127.0.0.1:8080";

    private final ConfigService configService;

    @Autowired
    public HandlerUrlService(final ConfigService configService) {
        this.configService = configService;
    }

    public String getServerUrl() {
        String configuredUrl = configService.getConfig().getHandlerUrl();
        return StringUtils.isEmpty(configuredUrl) ? DEFAULT_HOSTNAME : configuredUrl;
    }
}
