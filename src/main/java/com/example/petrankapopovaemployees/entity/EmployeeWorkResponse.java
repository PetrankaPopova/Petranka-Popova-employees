package com.example.petrankapopovaemployees.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
/**
 * Represents the response object containing information about employee projects and the longest working pair.
 */
@Data
@AllArgsConstructor
public class EmployeeWorkResponse {

    /** The list of employee projects. */
    private List<EmployeeProject> employeeProjects;

    /** The longest working pair of employees. */
    private EmployeePair longestWorkingPair;

    /**
     * Constructs a new EmployeeWorkResponse object.
     */
    public EmployeeWorkResponse() {
        // Default constructor
    }
}