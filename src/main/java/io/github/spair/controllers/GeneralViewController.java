package io.github.spair.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeneralViewController {

    @GetMapping("/config")
    public String config() {
        return "config";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
