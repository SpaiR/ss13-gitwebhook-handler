package io.github.spair.controller;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeneralViewControllerTest {

    private GeneralViewController controller;

    @Before
    public void setUp() {
        controller = new GeneralViewController();
    }

    @Test
    public void testConfig() {
        assertEquals("config", controller.config());
    }

    @Test
    public void testLogin() {
        assertEquals("login", controller.login());
    }
}