package io.github.spair.service;

import io.github.spair.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class TimeService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.YYYY");

    private final ConfigService configService;

    @Autowired
    public TimeService(final ConfigService configService) {
        this.configService = configService;
    }

    public String getCurrentDate() {
        ZoneId zoneId = ZoneId.of(configService.getConfig().getTimeZone());
        return LocalDate.now(zoneId).format(FORMATTER);
    }
}
