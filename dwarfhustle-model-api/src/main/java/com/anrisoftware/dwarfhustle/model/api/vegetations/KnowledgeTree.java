package com.anrisoftware.dwarfhustle.model.api.vegetations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Plant with an elongated stem, or trunk, usually supporting branches and
 * leaves.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeTree extends KnowledgeVegetation {

    public static final int OBJECT_TYPE = KnowledgeTree.class.getSimpleName().hashCode();

    public static final String TYPE = "Tree";

    public KnowledgeTree(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return KnowledgeTree.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeTree.TYPE;
    }

}
