package com.step;

import org.springframework.batch.item.ItemProcessor;

public class EmployeeProcessor implements ItemProcessor<Employee, Employee> {
    public Employee process(Employee employee) throws Exception {
        System.out.println("Inserting employee : " + employee);
        return employee;
    }
}
