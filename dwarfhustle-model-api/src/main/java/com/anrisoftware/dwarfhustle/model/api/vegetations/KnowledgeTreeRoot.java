package com.anrisoftware.dwarfhustle.model.api.vegetations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Root of the tree.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeTreeRoot extends KnowledgeTree {

    public static final int OBJECT_TYPE = KnowledgeTree.class.getSimpleName().hashCode();

    public static final String TYPE = "Tree-Root";

    public KnowledgeTreeRoot(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return KnowledgeTreeRoot.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeTreeRoot.TYPE;
    }

}
