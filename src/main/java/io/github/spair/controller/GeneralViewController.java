package io.github.spair.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeneralViewController {

    private static final String VERSION_PROP = "appVersion";
    private final String applicationVersion;

    @Autowired
    public GeneralViewController(final BuildProperties buildProperties) {
        applicationVersion = buildProperties.getVersion();
    }

    @GetMapping("/config")
    public String config(final Model model) {
        model.addAttribute(VERSION_PROP, applicationVersion);
        return "config";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
