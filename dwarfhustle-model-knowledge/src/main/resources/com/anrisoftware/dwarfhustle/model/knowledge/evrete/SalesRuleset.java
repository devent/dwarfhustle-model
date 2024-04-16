package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.Where;

import com.anrisoftware.dwarfhustle.model.knowledge.evrete.Customer;
import com.anrisoftware.dwarfhustle.model.knowledge.evrete.Invoice;

public class SalesRuleset {

    @Rule
    public void rule1(Customer $c) {
        $c.setTotal(0.0);
    }

    @Rule
    @Where("$i.customer == $c")
    public void rule2(Customer $c, Invoice $i) {
        $c.addToTotal($i.getAmount());
    }
}
