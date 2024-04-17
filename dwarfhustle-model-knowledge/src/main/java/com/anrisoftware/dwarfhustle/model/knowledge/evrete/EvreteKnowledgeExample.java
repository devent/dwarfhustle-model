/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
