package com.dynii.prototype.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleController {

    @GetMapping("/postcode")
    public String postcode() {
        return "postcode";
    }
}
