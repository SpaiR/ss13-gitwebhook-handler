package io.github.spair.service;

import io.github.spair.service.TimeService;
import io.github.spair.service.config.ConfigService;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

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