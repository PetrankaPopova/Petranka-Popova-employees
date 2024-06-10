package com.example.petrankapopovaemployees.controller;

import com.example.petrankapopovaemployees.entity.EmployeePair;
import com.example.petrankapopovaemployees.entity.EmployeeProject;
import com.example.petrankapopovaemployees.entity.EmployeeWorkResponse;
import com.example.petrankapopovaemployees.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
/**
 * Controller class for managing employee-related operations.
 */
@Controller
public class EmployeeController {

    /**
     * Service responsible for handling employee-related operations.
     */
    private final EmployeeService employeeService;

    /**
     * Constructor to initialize the EmployeeController with an EmployeeService.
     *
     * @param employeeService The EmployeeService instance to be used by the controller.
     */
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Displays the upload form for users to submit employee data files.
     *
     * @param model The model to be populated with data for the view.
     * @return The name of the view template to be rendered.
     */
    @GetMapping("/upload")
    public String showUploadForm() {
        return "home";
    }

    /**
     * Processes the uploaded file containing employee data.
     * This method handles POST requests to upload employee data files, determines the file type,
     * and delegates the processing to the appropriate service.
     *
     * @param file The MultipartFile representing the uploaded file.
     * @return ResponseEntity with the result of processing the file.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> processUploadedFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required.");
        }

        try {
            EmployeeWorkResponse response = new EmployeeWorkResponse();
            List<EmployeeProject> employeeProjects = employeeService.loadEmployeeProjects(file);
            response.setEmployeeProjects(employeeProjects);
            EmployeePair longestWorkingPair = employeeService.processFile(file);
            response.setLongestWorkingPair(longestWorkingPair);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while processing the file.");
        }
    }
}


