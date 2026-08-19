package com.example.New_Project.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private/admin")
public class AdminController {

    @GetMapping
    public String adminAccess() {
        return "Hello Admin";
    }
}