package com.anrisoftware.dwarfhustle.model.api.vegetations;

/**
 * Leaf of the tree.
 */
public class KnowledgeTreeLeaf extends KnowledgeTree {

    public static final int OBJECT_TYPE = KnowledgeTree.class.getSimpleName().hashCode();

    public static final String TYPE = "Tree-Leaf";

    public KnowledgeTreeLeaf(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return KnowledgeTreeLeaf.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeTreeLeaf.TYPE;
    }

}
