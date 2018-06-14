package io.github.spair.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.info.BuildProperties;
import org.springframework.ui.Model;

import static org.junit.Assert.assertEquals;

public class GeneralViewControllerTest {

    private GeneralViewController controller;

    @Before
    public void setUp() {
        controller = new GeneralViewController(Mockito.mock(BuildProperties.class));
    }

    @Test
    public void testConfig() {
        assertEquals("config", controller.config(Mockito.mock(Model.class)));
    }

    @Test
    public void testLogin() {
        assertEquals("login", controller.login());
    }
}