package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import lombok.Data;

@Data
public class Invoice {
    private final Customer customer;
    private final double amount;

    public Invoice(Customer customer, double amount) {
        this.customer = customer;
        this.amount = amount;
    }
    // getters and setters
}
