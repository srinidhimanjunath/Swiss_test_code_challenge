package com.big.company.employee.repository;

import java.util.HashMap;
import java.util.Map;

import com.big.company.employee.model.Employee;

public class EmployeeRepository {
	
	private final Map<Integer, Employee> employees = new HashMap<>();
    
    
    public void addEmployee(Employee employee) {
    	employees.put(employee.getId(), employee);
	}
    
    
    public Map<Integer, Employee> getAllEmployees() {
		return employees;
	}
    
    public Employee getEmployeeById(int id) {
		return employees.get(id);
	}

  
}
