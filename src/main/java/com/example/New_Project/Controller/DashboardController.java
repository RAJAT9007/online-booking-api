package com.example.New_Project.Controller;

import com.example.New_Project.Service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

}
