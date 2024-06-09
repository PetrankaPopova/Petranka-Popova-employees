package com.example.petrankapopovaemployees.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class EmployeeWorkResponse {
    private List<EmployeeProject> employeeProjects;
    private List<EmployeePair> longestWorkingPair;

    public EmployeeWorkResponse() {

    }
}