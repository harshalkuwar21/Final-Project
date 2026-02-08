package com.Dk3.Cars.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestDrivesController {

    @GetMapping("/testdrives")
    public String testDrivesPage() {
        return "testdrives";
    }
}