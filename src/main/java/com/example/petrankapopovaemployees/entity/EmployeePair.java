package com.example.petrankapopovaemployees.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Entity class representing a pair of employees who have worked together.
 * This class stores the IDs of the two employees and the number of days they have worked together on common projects.
 */
@Data
@AllArgsConstructor
public class EmployeePair {

    public EmployeePair(){

    }
    /**
     * The ID of the first employee in the pair.
     */
    private long employeeId1;

    /**
     * The ID of the second employee in the pair.
     */
    private long employeeId2;

    /**
     * The number of days the two employees have worked together on common projects.
     */
    private long daysWorkedTogether;
    private int projectId;


}
