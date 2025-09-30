package com.big.company.employee;

import com.big.company.employee.repository.EmployeeRepository;
import com.big.company.employee.service.EmployeeService;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.print(
					"Please provide the absolute path to the csv file.. The correct format to run the project : java -jar <jar.jar> <absolute-path-to-csv>");
			System.exit(1);
		}

		EmployeeRepository employeeRepository = new EmployeeRepository();
		try {
			String fileName = args[0];
			System.out.println("Loading the file :" + fileName);
			EmployeeService employeeService = new EmployeeService();
			employeeService.loadFromCSV(fileName, employeeRepository);
			employeeService.validateEmployeeSalares(employeeRepository);
			employeeService.validateManagersWithMoreReporteesThanExpected(employeeRepository);
			
		} catch (Exception ex) {

		}

	}
}
