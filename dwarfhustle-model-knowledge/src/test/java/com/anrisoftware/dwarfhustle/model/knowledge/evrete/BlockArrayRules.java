package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.EMPTY;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.FILLED;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.VISIBLE;

import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;

public class BlockArrayRules {

    public static final short OXYGEN_ID = 923;

    public static final short STONE_ID = 852;

    public static final short LIQUID_ID = 930;

    public KnowledgeService service;

    public Knowledge initialKn;

    public Knowledge mapKn;

    public BlockArrayRules() {
        this.service = new KnowledgeService();
    }

    public void setupRulesInit() {
        this.initialKn = service.newKnowledge().builder().newRule().forEach("$fact", BlockFact.class)
                .where("$fact.z < 28").execute(context -> {
                    BlockFact fact = context.get("$fact");
                    var array = fact.array;
                    int x = fact.x;
                    int y = fact.y;
                    int z = fact.z;
                    array.setMaterial(x, y, z, OXYGEN_ID);
                    array.setProp(x, y, z, VISIBLE | EMPTY);
                    array.setTemp(x, y, z, (short) 30);
                }).newRule().forEach("$fact", BlockFact.class).where("$fact.z > 27").execute(context -> {
                    BlockFact fact = context.get("$fact");
                    var array = fact.array;
                    int x = fact.x;
                    int y = fact.y;
                    int z = fact.z;
                    array.setMaterial(x, y, z, STONE_ID);
                    array.setProp(x, y, z, VISIBLE | FILLED);
                    array.setTemp(x, y, z, (short) 30);
                }).build();
    }

    public void setupRulesMap() {
        this.mapKn = service.newKnowledge().builder().newRule().forEach("$fact", BlockFact.class).where("$fact.z < 28")
                .execute(context -> {
                    BlockFact fact = context.get("$fact");
                    var array = fact.array;
                    int x = fact.x;
                    int y = fact.y;
                    int z = fact.z;
                    array.setMaterial(x, y, z, OXYGEN_ID);
                    array.setProp(x, y, z, VISIBLE | EMPTY);
                    array.setTemp(x, y, z, (short) 30);
                }).newRule().forEach("$fact", BlockFact.class).where("$fact.z > 27").execute(context -> {
                    BlockFact fact = context.get("$fact");
                    var array = fact.array;
                    int x = fact.x;
                    int y = fact.y;
                    int z = fact.z;
                    array.setMaterial(x, y, z, STONE_ID);
                    array.setProp(x, y, z, VISIBLE | FILLED);
                    array.setTemp(x, y, z, (short) 30);
                }).build();
    }

}
