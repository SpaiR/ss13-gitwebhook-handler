package io.github.spair.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeneralViewControllerTest {

    @Test
    public void testConfig() {
        GeneralViewController controller = new GeneralViewController();
        assertEquals("config", controller.config());
    }

    @Test
    public void testLogin() {
        GeneralViewController controller = new GeneralViewController();
        assertEquals("login", controller.login());
    }
}