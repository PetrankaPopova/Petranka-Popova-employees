package com.example.petrankapopovaemployees.service;

import com.example.petrankapopovaemployees.entity.EmployeePair;
import com.example.petrankapopovaemployees.entity.EmployeeProject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    public List<EmployeePair> processFile(MultipartFile file) throws IOException, CsvValidationException {
        List<EmployeeProject> employeeProjects = loadEmployeeProjects(file);
        if (employeeProjects.isEmpty()) {
            logger.error("Failed to load employee projects.");
            return Collections.emptyList();
        }
        return findEmployeePairs(employeeProjects);
    }

    public List<EmployeeProject> loadEmployeeProjects(MultipartFile file) throws IOException, CsvValidationException {
        List<EmployeeProject> employeeProjects = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 4) continue;
                try {
                    Integer empId = Integer.parseInt(line[0].trim());
                    Integer projectId = Integer.parseInt(line[1].trim());
                    LocalDate dateFrom = parseDate(line[2].trim());
                    LocalDate dateTo = line[3].trim().equalsIgnoreCase("NULL") ? LocalDate.now() : parseDate(line[3].trim());
                    employeeProjects.add(new EmployeeProject(empId, projectId, dateFrom, dateTo));
                } catch (NumberFormatException e) {
                    logger.warn("Skipping non-numeric value: {}", e.getMessage());
                }
            }
        }
        return employeeProjects;
    }

    private LocalDate parseDate(String dateStr) {
        List<String> dateFormats = Arrays.asList("yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy");
        for (String format : dateFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                logger.debug("Date format {} did not match for date {}", format, dateStr);
            }
        }
        throw new IllegalArgumentException("Unknown date format: " + dateStr);
    }

    private List<EmployeePair> findEmployeePairs(List<EmployeeProject> employeeProjects) {
        Map<String, Long> pairDurationMap = new HashMap<>();

        for (int i = 0; i < employeeProjects.size(); i++) {
            for (int j = i + 1; j < employeeProjects.size(); j++) {
                EmployeeProject ep1 = employeeProjects.get(i);
                EmployeeProject ep2 = employeeProjects.get(j);

                if (ep1.getProjectId()==(ep2.getProjectId())) {
                    long overlapDays = calculateOverlapDays(ep1, ep2);
                    if (overlapDays > 0) {
                        String pairKey = ep1.getEmpId() < ep2.getEmpId() ? ep1.getEmpId() + "," + ep2.getEmpId() : ep2.getEmpId() + "," + ep1.getEmpId();
                        pairDurationMap.put(pairKey, pairDurationMap.getOrDefault(pairKey, 0L) + overlapDays);
                    }
                }
            }
        }

        List<EmployeePair> employeePairs = new ArrayList<>();
        for (Map.Entry<String, Long> entry : pairDurationMap.entrySet()) {
            String[] ids = entry.getKey().split(",");
            employeePairs.add(new EmployeePair(Long.parseLong(ids[0]), Long.parseLong(ids[1]), entry.getValue()));
        }

        return employeePairs;
    }

    private static long calculateOverlapDays(EmployeeProject ep1, EmployeeProject ep2) {
        LocalDate start = ep1.getDateFrom().isAfter(ep2.getDateFrom()) ? ep1.getDateFrom() : ep2.getDateFrom();
        LocalDate end = ep1.getDateTo().isBefore(ep2.getDateTo()) ? ep1.getDateTo() : ep2.getDateTo();

        if (end.isBefore(start)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(start, end) + 1; // Add 1 to include the end day in the count
    }
}

