package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.DISCOVERED;

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
                .where("$fact.z == 0") //
                .execute(context -> {
                    BlockFact fact = context.get("$fact");
                    int p = BlockArray.getProp(fact.chunk, fact.x, fact.y, fact.z);
                    BlockArray.setProp(fact.chunk, fact.x, fact.y, fact.z, p | DISCOVERED);
                }) //
                .newRule() //
                .salience(1000) //
                .forEach("$fact", BlockFact.class) //
                .where("$fact.z > 0") //
                .execute(context -> {
                    BlockFact fact = context.get("$fact");
                    int p = BlockArray.getProp(fact.chunk, fact.x, fact.y, fact.z);
                    BlockArray.setProp(fact.chunk, fact.x, fact.y, fact.z, p | DISCOVERED);
                }) //
                .build();
    }

}
