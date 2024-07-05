package com.anrisoftware.dwarfhustle.model.api.vegetations;

/**
 * Branch of the tree.
 */
public class KnowledgeTreeBranch extends KnowledgeTree {

    public static final String OBJECT_TYPE = KnowledgeTree.class.getSimpleName();

    public static final String TYPE = "Tree-Branch";

    public KnowledgeTreeBranch(int kid) {
        super(kid);
    }

    @Override
    public String getObjectType() {
        return KnowledgeTreeBranch.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeTreeBranch.TYPE;
    }

}
