

package com.smart_campus_hub.smart_campus_api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "Smart Campus API is running 🚀";
    }

    @GetMapping("/test")
    public String test() {
        return "Backend is working correctly";
    }
}