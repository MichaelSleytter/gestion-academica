package com.example.gestionacademica.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScalarController {

    @GetMapping({"/scalar", "/scalar/"})
    public String scalar() {
        return "forward:/scalar.html";
    }
}