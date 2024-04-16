package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import org.evrete.dsl.annotation.Fact;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.Where;

public class SalesRuleset {

    @Rule
    public void rule1(@Fact("$c") Customer $c) {
        $c.setTotal(0.0);
    }

    @Rule
    @Where("$i.customer == $c")
    public void rule2(@Fact("$c") Customer $c, @Fact("$i") Invoice $i) {
        $c.addToTotal($i.getAmount());
    }
}
