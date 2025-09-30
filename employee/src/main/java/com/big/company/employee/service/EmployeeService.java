package com.big.company.employee.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.big.company.employee.constants.EmployeeConstants;
import com.big.company.employee.model.Employee;
import com.big.company.employee.repository.EmployeeRepository;

public class EmployeeService {
	
	private static int reporteesLevel = 0;

	public void loadFromCSV(String filename, EmployeeRepository employeeRepository) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String row = reader.readLine(); // skip header
			while ((row = reader.readLine()) != null) {
				String[] employeeData = row.split(",");
				int id = Integer.parseInt(employeeData[0]);
				String firstName = employeeData[1];
				String lastName = employeeData[2];
				double salary = Double.parseDouble(employeeData[3]);
				// Check if the employee has a manager and add the data accordingly
				Integer managerId = employeeData.length > 4 && !employeeData[4].isEmpty()
						? Integer.parseInt(employeeData[4])
						: null;

				Employee employee = new Employee(id, firstName, lastName, salary, managerId);
				employeeRepository.addEmployee(employee);
			}
		}

		for (Employee employee : employeeRepository.getAllEmployees().values()) {
			if (employee.getManagerId() != null) {
				Employee manager = employeeRepository.getEmployeeById(employee.getManagerId());
				if (manager != null) {
					manager.addDirectReportees(employee);
				}
			} 
		}
	}

	public void validateEmployeeSalares(EmployeeRepository employeeRepository) {
		for (Employee employee : employeeRepository.getAllEmployees().values()) {

			List<Employee> directReportees = employee.getDirectReportees();
			if (directReportees == null || directReportees.isEmpty()) {
				continue;
			}

			double avgSalariesOfReportees = directReportees.stream().mapToDouble(Employee::getSalary).average()
					.orElse(0);

			double minSalaryThatManagerHasToMake = avgSalariesOfReportees * 1.2;
			double maxSalaryThatManagerCanMake = avgSalariesOfReportees * 1.5;
			double managerSalary = employee.getSalary();

			if (managerSalary < minSalaryThatManagerHasToMake) {
				double differenceInSalary = minSalaryThatManagerHasToMake - managerSalary;
				System.out.println("Manager : " + employee.getFirstName() + " " + employee.getLastName()  +"is making less than the expected salaray by : " + differenceInSalary);
			} else if (managerSalary > maxSalaryThatManagerCanMake) {
				double differenceInSalary = managerSalary - maxSalaryThatManagerCanMake;
				System.out.println("Manager : " + employee.getFirstName() + " " + employee.getLastName()  +"is making more than the expected salaray by : " + differenceInSalary);
			}
		}
	}

	public void validateManagersWithMoreReporteesThanExpected(EmployeeRepository employeeRepository) {
		
		for(Employee employee : employeeRepository.getAllEmployees().values()) {
			reporteesLevel = 0;
			navigateThroughTheReportees(employee);
			//System.out.println("Total reportees for the employee :" + employee.getFirstName() + " " + reporteesLevel);
			if( reporteesLevel > EmployeeConstants.MAX_REPORTING_LEVELS) {
				System.out.println("Manager : " + employee.getFirstName() + " " + employee.getLastName()
				+ " " + (reporteesLevel - EmployeeConstants.MAX_REPORTING_LEVELS) + " number of more reoprtees than expected"  );
			}
			
		}
		
		
	}

	private void navigateThroughTheReportees(Employee employee) {
	
		for (Employee reportee : employee.getDirectReportees()) {
			reporteesLevel++;
			navigateThroughTheReportees(reportee);
		}
		

	}

}
