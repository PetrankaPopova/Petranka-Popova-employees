package com.example.petrankapopovaemployees.controller;

import com.example.petrankapopovaemployees.entity.EmployeePair;
import com.example.petrankapopovaemployees.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Endpoint to process the uploaded file and find employee pairs who worked together.
     *
     * @param filePath the path to the CSV file
     * @return ResponseEntity with the list of employee pairs or an error message
     */
    @PostMapping("/upload")
    public ResponseEntity<?> processUploadedFile(@RequestParam(required = false, value = "filePath") String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("File path is required.");
        }

        try {
            List<EmployeePair> employeePairs = employeeService.manageFile(filePath);
            if (employeePairs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No employee pairs found.");
            }
            return ResponseEntity.ok(employeePairs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the file.");
        }
    }
}