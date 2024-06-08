package com.example.petrankapopovaemployees.controller;

import com.example.petrankapopovaemployees.entity.EmployeePair;
import com.example.petrankapopovaemployees.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


import java.util.List;

@Controller
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "home";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> processUploadedFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            ModelAndView mav = new ModelAndView("home");
            mav.addObject("error", "File is required.");
            return ResponseEntity.badRequest().body(mav);
        }

        try {
            List<EmployeePair> employeePairs = employeeService.processFile(file);
            if (employeePairs.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(employeePairs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while processing the file.");
        }
    }
}
