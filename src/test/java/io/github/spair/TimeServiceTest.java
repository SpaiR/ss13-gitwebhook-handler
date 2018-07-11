package io.github.spair;

import io.github.spair.service.config.ConfigService;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeServiceTest {

    @Test
    public void testGetCurrentDate() {
        ConfigService configService = mock(ConfigService.class, RETURNS_DEEP_STUBS);
        when(configService.getConfig().getTimeZone()).thenReturn("Europe/Moscow");

        TimeService timeService = new TimeService(configService);

        String expectedDate = LocalDate.now(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("dd.MM.YYYY"));
        assertEquals(expectedDate, timeService.getCurrentDate());
    }
}