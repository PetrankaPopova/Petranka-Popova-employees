package com.example.petrankapopovaemployees;

import com.example.petrankapopovaemployees.entity.EmployeePair;
import com.example.petrankapopovaemployees.entity.EmployeeProject;
import com.example.petrankapopovaemployees.service.EmployeeService;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmployeeServiceTest {
    private final EmployeeService employeeService = new EmployeeService();

    @Test
    public void testLoadEmployeeProjects_withValidDataInput() throws IOException, CsvValidationException {
        String csvContent = "143,12,2013-01-11,2014-05-01\n" +
                "218,10,2012-05-16,NULL\n" +
                "143,10,2009-01-01,2012-05-27";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        List<EmployeeProject> projects = employeeService.loadEmployeeProjects(file);

        assertEquals(3, projects.size());
        assertEquals(143, projects.get(0).getEmpId());
        assertEquals(12, projects.get(0).getProjectId());
        assertEquals(LocalDate.of(2013, 1, 11), projects.get(0).getDateFrom());
        assertEquals(LocalDate.of(2014, 5, 1), projects.get(0).getDateTo());

        assertEquals(218, projects.get(1).getEmpId());
        assertEquals(10, projects.get(1).getProjectId());
        assertEquals(LocalDate.of(2012, 5, 16), projects.get(1).getDateFrom());
        assertEquals(LocalDate.now(), projects.get(1).getDateTo()); // Assuming NULL means current date

        assertEquals(143, projects.get(2).getEmpId());
        assertEquals(10, projects.get(2).getProjectId());
        assertEquals(LocalDate.of(2009, 1, 1), projects.get(2).getDateFrom());
        assertEquals(LocalDate.of(2012, 5, 27), projects.get(2).getDateTo());
    }

    @Test
    public void testLoadEmployeeProjects_withInvalidDate() throws IOException, CsvValidationException {
        String csvContent = "143,12,invalid,5/1/2014";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        List<EmployeeProject> projects = employeeService.loadEmployeeProjects(file);

        assertEquals(0, projects.size()); // Skipped due to invalid date
    }

    @Test
    public void testLoadEmployeeProjects_withEndBeforeStartDate() throws IOException, CsvValidationException {
        String csvContent = "143,12,5/1/2014,1/11/2013";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        List<EmployeeProject> projects = employeeService.loadEmployeeProjects(file);

        assertEquals(0, projects.size()); // Skipped due to end before start
    }

    @Test
    public void testProcessFile_withOverlappingProjects() throws IOException, CsvValidationException {
        String csvContent = "143,12,2013-01-11,2014-05-01\n" +
                "143,10,2012-05-16,2013-05-16\n" +
                "143,10,2009-01-01,2012-05-27";

        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        EmployeePair pair = employeeService.processFile(file);

        assertEquals(143, pair.getEmployeeId1());
        assertEquals(143, pair.getEmployeeId2());
        assertEquals(12, pair.getDaysWorkedTogether());
    }

    @Test
    public void testLoadEmployeeProjects_withEmptyFile() throws IOException, CsvValidationException {
        String csvContent = "";
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        List<EmployeeProject> projects = employeeService.loadEmployeeProjects(file);

        assertEquals(0, projects.size());
    }

    @Test
    public void testLoadEmployeeProjects_withNullEndDate() throws IOException, CsvValidationException {
        String csvContent = "143,12,2013-01-11,NULL";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        List<EmployeeProject> projects = employeeService.loadEmployeeProjects(file);

        assertEquals(1, projects.size());
        assertEquals(LocalDate.of(2013, 1, 11), projects.get(0).getDateFrom());
        assertEquals(LocalDate.now(), projects.get(0).getDateTo()); // Assuming NULL means current date
    }

    @Test
    public void testProcessFile_withNonOverlappingProjects() throws IOException, CsvValidationException {
        String csvContent = "143,12,1/11/2013,5/1/2013\n" +
                "218,12,6/1/2013,5/1/2014";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        EmployeePair pair = employeeService.processFile(file);

        assertEquals(null, pair);
    }

    @Test
    public void testLoadEmployeeProjects_withFutureStartDate() throws IOException, CsvValidationException {
        String csvContent = "143,12,2030-01-11,2031-05-01";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        List<EmployeeProject> projects = employeeService.loadEmployeeProjects(file);

        assertEquals(1, projects.size());
        assertEquals(LocalDate.of(2030, 1, 11), projects.get(0).getDateFrom());
        assertEquals(LocalDate.of(2031, 5, 1), projects.get(0).getDateTo());
    }

    @Test
    public void testLoadEmployeeProjects_withNonNumericValues() throws IOException, CsvValidationException {
        String csvContent = "abc,12,1/11/2013,5/1/2014\n" +
                "143,xyz,1/11/2013,5/1/2014";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        List<EmployeeProject> projects = employeeService.loadEmployeeProjects(file);

        assertEquals(0, projects.size()); // Skipped due to non-numeric values
    }

    @Test
    public void testProcessFile_withMultipleOverlappingPairs() throws IOException, CsvValidationException {
        String csvContent = "143,12,2013-01-11,2014-05-01\n" +
                "218,12,2013-05-01,2014-05-01\n" +
                "143,10,2009-01-01,2012-05-27\n" +
                "218,10,2009-01-01,2012-05-27";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        EmployeePair pair = employeeService.processFile(file);

        assertTrue(pair != null && pair.getEmployeeId1() == 143 && pair.getEmployeeId2() == 218 && pair.getDaysWorkedTogether() > 1L);
    }
}