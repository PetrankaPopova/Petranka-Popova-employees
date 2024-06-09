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
/**
 * Service class to handle operations related to employee projects.
 */
@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    /**
     * Processes the uploaded file to find the longest working pair of employees.
     *
     * @param file The uploaded CSV file containing employee project data.
     * @return The longest working pair of employees, or null if not found.
     * @throws IOException           If an I/O error occurs while reading the file.
     * @throws CsvValidationException If an error occurs during CSV validation.
     */
    public EmployeePair processFile(MultipartFile file) throws IOException, CsvValidationException {
        List<EmployeeProject> employeeProjects = loadEmployeeProjects(file);
        if (employeeProjects.isEmpty()) {
            logger.error("Failed to load employee projects.");
            return null;
        }
        return findEmployeePairs(employeeProjects);
    }

    /**
     * Loads employee project data from the uploaded CSV file.
     *
     * @param file The uploaded CSV file containing employee project data.
     * @return A list of employee project objects.
     * @throws IOException           If an I/O error occurs while reading the file.
     * @throws CsvValidationException If an error occurs during CSV validation.
     */
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

                    if (dateTo.isBefore(dateFrom)) {
                        logger.warn("Invalid date range: {} - {}", dateFrom, dateTo);
                        continue;
                    }

                    employeeProjects.add(new EmployeeProject(empId, projectId, dateFrom, dateTo));
                } catch (NumberFormatException e) {
                    logger.warn("Skipping non-numeric value: {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.warn("Skipping invalid date: {}", e.getMessage());
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

    private EmployeePair findEmployeePairs(List<EmployeeProject> employeeProjects) {
        employeeProjects.sort(Comparator.comparing(EmployeeProject::getDateFrom));

        Map<String, Long> pairDurationMap = new HashMap<>();
        TreeMap<LocalDate, List<EmployeeProject>> activeProjects = new TreeMap<>();

        for (EmployeeProject currentProject : employeeProjects) {
            LocalDate currentStart = currentProject.getDateFrom();
            LocalDate currentEnd = currentProject.getDateTo();

            activeProjects.headMap(currentStart, false).clear();

            activeProjects.computeIfAbsent(currentEnd, k -> new ArrayList<>()).add(currentProject);

            for (List<EmployeeProject> projects : activeProjects.values()) {
                for (EmployeeProject activeProject : projects) {
                    if (activeProject.getProjectId() == currentProject.getProjectId() && !activeProject.equals(currentProject)) {
                        long overlapDays = calculateOverlapDays(activeProject, currentProject);
                        if (overlapDays > 0) {
                            String pairKey = activeProject.getEmpId() < currentProject.getEmpId()
                                    ? activeProject.getEmpId() + "," + currentProject.getEmpId()
                                    : currentProject.getEmpId() + "," + activeProject.getEmpId();
                            pairDurationMap.put(pairKey, pairDurationMap.getOrDefault(pairKey, 0L) + overlapDays);
                        }
                    }
                }
            }
        }

        EmployeePair employeePairs = new EmployeePair();
        for (Map.Entry<String, Long> entry : pairDurationMap.entrySet()) {
            String[] ids = entry.getKey().split(",");
            employeePairs.setEmployeeId1(Long.parseLong(ids[0]));
            employeePairs.setEmployeeId2(Long.parseLong(ids[1]));
            employeePairs.setDaysWorkedTogether(entry.getValue());

        }

        return employeePairs;
    }

    private static long calculateOverlapDays(EmployeeProject ep1, EmployeeProject ep2) {
        LocalDate start = ep1.getDateFrom().isAfter(ep2.getDateFrom()) ? ep1.getDateFrom() : ep2.getDateFrom();
        LocalDate end = ep1.getDateTo().isBefore(ep2.getDateTo()) ? ep1.getDateTo() : ep2.getDateTo();

        if (end.isBefore(start)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}