package com.example.petrankapopovaemployees.service;

import com.example.petrankapopovaemployees.entity.EmployeePair;
import com.example.petrankapopovaemployees.entity.EmployeeProject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class EmployeeService {
        private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

        /**
         * Processes the given file to find employee pairs who have worked together.
         *
         * @param filePath the path to the CSV file
         * @return a list of employee pairs who have worked together
         */
        public List<EmployeePair> manageFile(String filePath) {
            List<EmployeeProject> employeeProjects = loadEmployeeProjects(filePath);
            if (employeeProjects.isEmpty()) {
                logger.error("Failed to load employee projects.");
                return Collections.emptyList();
            }
            return findEmployeePairs(employeeProjects);
        }

        /**
         * Loads employee projects from a CSV file.
         *
         * @param filePath the path to the CSV file
         * @return a list of employee projects
         */
        private List<EmployeeProject> loadEmployeeProjects(String filePath) {
            List<EmployeeProject> employeeProjects = new ArrayList<>();
            try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
                String[] line;
                while ((line = reader.readNext()) != null) {
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
            } catch (IOException | CsvValidationException e) {
                logger.error("Error reading CSV file", e);
                return Collections.emptyList();
            }
            return employeeProjects;
        }

        /**
         * Parses a date string into a LocalDate object.
         *
         * @param dateStr the date string to parse
         * @return the parsed LocalDate object
         */
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

        /**
         * Finds pairs of employees who have worked together on common projects.
         *
         * @param employeeProjects the list of employee projects
         * @return a list of employee pairs with the number of days they have worked together
         */
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

        /**
         * Calculates the number of overlapping days between two employee projects.
         *
         * @param ep1 the first employee project
         * @param ep2 the second employee project
         * @return the number of overlapping days
         */
        private long calculateOverlapDays(EmployeeProject ep1, EmployeeProject ep2) {
            LocalDate start = ep1.getDateFrom().isAfter(ep2.getDateFrom()) ? ep1.getDateFrom() : ep2.getDateFrom();
            LocalDate end = ep1.getDateTo().isBefore(ep2.getDateTo()) ? ep1.getDateTo() : ep2.getDateTo();
            return ChronoUnit.DAYS.between(start, end) + 1;
        }
    }
