package com.example.petrankapopovaemployees.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entity class representing a project assigned to an employee.
 * This class stores the employee ID, project ID, start date, and end date of the project.
 */
@Data
@AllArgsConstructor
public class EmployeeProject {
    /**
     * The ID of the employee assigned to the project.
     */
    private Integer empId;

    /**
     * The ID of the project.
     */
    private Integer projectId;

    /**
     * The start date of the project.
     */
    private LocalDate dateFrom;

    /**
     * The end date of the project.
     * If the project is ongoing, this field may contain a future date or be set to null.
     */
    private LocalDate dateTo;
}