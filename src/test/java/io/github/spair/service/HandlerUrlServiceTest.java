package io.github.spair.service;

import io.github.spair.service.config.ConfigService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HandlerUrlServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    private HandlerUrlService handlerUrlService;

    @Before
    public void setUp() {
        handlerUrlService = new HandlerUrlService(configService);
        when(configService.getConfig().getHandlerUrl()).thenReturn("localhost:8090");
    }

    @Test
    public void testGetServerUrl() {
        assertEquals("localhost:8090", handlerUrlService.getServerUrl());
    }
}