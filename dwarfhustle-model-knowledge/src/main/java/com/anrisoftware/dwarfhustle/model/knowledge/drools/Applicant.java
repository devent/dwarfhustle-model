package com.anrisoftware.dwarfhustle.model.knowledge.drools;

import lombok.Data;

@Data
public class Applicant {

    private String name;
    private int age;
    private double currentSalary;
    private int experienceInYears;

    public Applicant(String name, int age, Double currentSalary, int experienceInYears) {
        this.name = name;
        this.age = age;
        this.currentSalary = currentSalary;
        this.experienceInYears = experienceInYears;
    }
}
