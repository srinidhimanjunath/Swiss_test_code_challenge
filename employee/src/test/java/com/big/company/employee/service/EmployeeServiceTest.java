package com.big.company.employee.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.big.company.employee.model.Employee;
import com.big.company.employee.repository.EmployeeRepository;
import com.big.company.employee.constants.EmployeeConstants;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class EmployeeServiceTest {

    private EmployeeService service;
    private EmployeeRepository repository;

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        service = new EmployeeService();
        repository = new EmployeeRepository();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    @DisplayName("loadFromCSV should populate repository and build manager-reportee relationships")
    void testLoadFromCSVBuildsRelationships(@TempDir Path tempDir) throws IOException {
        File csv = tempDir.resolve("employees.csv").toFile();
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(csv))) {
            bw.write("id,first,last,salary,managerId\n");
            bw.write("1,John,Doe,100000,\n");
            bw.write("2,Jane,Smith,60000,1\n");
            bw.write("3,Bob,Brown,65000,1\n");
        }

        service.loadFromCSV(csv.getAbsolutePath(), repository);

        assertEquals(3, repository.getAllEmployees().size());
        Employee manager = repository.getEmployeeById(1);
        assertNotNull(manager);
        List<Employee> reps = manager.getDirectReportees();
        assertEquals(2, reps.size());
        assertTrue(reps.stream().anyMatch(e -> e.getFirstName().equals("Jane")));
        assertTrue(reps.stream().anyMatch(e -> e.getFirstName().equals("Bob")));
    }

    @Test
    @DisplayName("validateEmployeeSalares prints when manager below minimum")
    void testValidateEmployeeSalariesBelowMin() {
        Employee manager = new Employee(1, "Manager", "Low", 100.0, null); // avg reportees 100 -> min 120
        Employee r1 = new Employee(2, "Rep1", "A", 100.0, 1);
        manager.addDirectReportees(r1);
        repository.addEmployee(manager);
        repository.addEmployee(r1);

        service.validateEmployeeSalares(repository);
        String output = outContent.toString();
        assertTrue(output.contains("less than the expected salaray"), () -> "Output was: " + output);
    }

    @Test
    @DisplayName("validateEmployeeSalares prints when manager above maximum")
    void testValidateEmployeeSalariesAboveMax() {
        resetOut();
        Employee manager = new Employee(1, "Manager", "High", 200.0, null); // avg 100 -> max 150
        Employee r1 = new Employee(2, "Rep1", "A", 100.0, 1);
        manager.addDirectReportees(r1);
        repository.addEmployee(manager);
        repository.addEmployee(r1);

        service.validateEmployeeSalares(repository);
        String output = outContent.toString();
        assertTrue(output.contains("more than the expected salaray"), () -> "Output was: " + output);
    }

    @Test
    @DisplayName("validateEmployeeSalares produces no output for manager within range")
    void testValidateEmployeeSalariesWithinRange() {
        resetOut();
        Employee manager = new Employee(1, "Manager", "Ok", 130.0, null); // avg 100 -> min 120 max 150
        Employee r1 = new Employee(2, "Rep1", "A", 100.0, 1);
        manager.addDirectReportees(r1);
        repository.addEmployee(manager);
        repository.addEmployee(r1);

        service.validateEmployeeSalares(repository);
        String output = outContent.toString();
        assertEquals("", output.trim(), () -> "Unexpected output: " + output);
    }

    @Test
    @DisplayName("validateManagersWithMoreReporteesThanExpected prints when levels exceed max")
    void testValidateManagersWithMoreReporteesThanExpected() {
        resetOut();
        // Build a chain deeper than MAX_REPORTING_LEVELS
        int chainLength = EmployeeConstants.MAX_REPORTING_LEVELS + 2; // ensures exceed by 1 at least
        Employee root = new Employee(1, "Root", "Manager", 150.0, null);
        repository.addEmployee(root);
        Employee prev = root;
        for(int i=2;i<=chainLength+1;i++) {
            Employee e = new Employee(i, "E"+i, "L", 100.0, prev.getId());
            prev.addDirectReportees(e);
            repository.addEmployee(e);
            prev = e;
        }

        service.validateManagersWithMoreReporteesThanExpected(repository);
        String output = outContent.toString();
        assertTrue(output.contains("number of more reoprtees than expected"), () -> "Output was: " + output);
    }

    private void resetOut() {
        System.setOut(originalOut); // restore to flush previous PrintStream
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }
}
