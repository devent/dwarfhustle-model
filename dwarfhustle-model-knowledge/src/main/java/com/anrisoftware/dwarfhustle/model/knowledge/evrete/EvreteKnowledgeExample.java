package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;

/**
 * Evrete example.
 */
public class EvreteKnowledgeExample {

    public Knowledge createKnowledge() {
        KnowledgeService service = new KnowledgeService();
        Knowledge knowledge = service.newKnowledge().builder() //
                .newRule("Clear total sales") //
                .forEach("$c", Customer.class) //
                .execute(ctx -> {
                    Customer c = ctx.get("$c");
                    c.setTotal(0.0);
                }) //
                .newRule("Compute totals") //
                .forEach("$c", Customer.class, "$i", Invoice.class) //
                .where("$i.customer == $c").execute(ctx -> {
                    Customer c = ctx.get("$c");
                    Invoice i = ctx.get("$i");
                    c.addToTotal(i.getAmount());
                }).build();
        return knowledge;
    }
}
