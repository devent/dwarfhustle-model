package com.anrisoftware.dwarfhustle.model.api.vegetations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Trunk of the tree.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeTreeTrunk extends KnowledgeTree {

    public static final String OBJECT_TYPE = KnowledgeTree.class.getSimpleName();

    public static final String TYPE = "Tree-Trunk";

    public KnowledgeTreeTrunk(int kid) {
        super(kid);
    }

    @Override
    public String getObjectType() {
        return KnowledgeTreeTrunk.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeTreeTrunk.TYPE;
    }

}
