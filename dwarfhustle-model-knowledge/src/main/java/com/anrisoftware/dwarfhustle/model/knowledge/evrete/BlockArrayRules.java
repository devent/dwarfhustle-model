package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;

public class BlockArrayRules {

    public static final short OXYGEN_ID = 923;

    public static final short STONE_ID = 852;

    public static final short LIQUID_ID = 930;

    public KnowledgeService service;

    public Knowledge mapKn;

    public BlockArrayRules() {
        this.service = new KnowledgeService();
    }

    public void setupRulesMap() {
        this.mapKn = service.newKnowledge().builder() //
                .newRule() //
                .salience(1000) //
                .forEach("$fact", BlockFact.class) //
                .where("$fact.chunk.changed && $fact.block.pos.z == 0") //
                .execute(context -> {
                    BlockFact fact = context.get("$fact");
                    fact.block.setDiscovered(true);
                }) //
                .newRule() //
                .salience(1000) //
                .forEach("$fact", BlockFact.class) //
                .where("$fact.chunk.changed && $fact.block.pos.z > 0 && $fact.block.getNeighborUp($fact.chunk, $fact.retriever).isEmpty() && $fact.block.isNeighborsUpEmptyContinuously($fact.chunk, $fact.retriever)") //
                .execute(context -> {
                    BlockFact fact = context.get("$fact");
                    fact.block.setDiscovered(true);
                    // System.out.println(fact.block); // TODO
                }) //
                .newRule() //
                .salience(1) //
                .forEach("$fact", BlockFact.class) //
                .where("$fact.chunk.changed") //
                .execute(context -> {
                    BlockFact fact = context.get("$fact");
                    fact.chunk.changed = false;
                }) //
                .build();
    }

}
