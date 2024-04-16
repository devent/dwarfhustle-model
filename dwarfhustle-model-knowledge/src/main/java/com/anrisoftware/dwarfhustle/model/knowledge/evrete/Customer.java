package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import lombok.Data;

@Data
public class Customer {
    private double total = 0.0;
    private final String name;

    public Customer(String name) {
        this.name = name;
    }

    public void addToTotal(double amount) {
        this.total += amount;
    }

}
