package com.accommodation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiInfoController {

    @GetMapping("/api-info")
    public String showApiGuide() {
        return "api-info";
    }
}
