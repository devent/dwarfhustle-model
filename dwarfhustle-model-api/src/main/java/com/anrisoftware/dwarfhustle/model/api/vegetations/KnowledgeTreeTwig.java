package com.anrisoftware.dwarfhustle.model.api.vegetations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Twig of the tree.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeTreeTwig extends KnowledgeTree {

    public static final String OBJECT_TYPE = KnowledgeTree.class.getSimpleName();

    public static final String TYPE = "Tree-Twig";

    public KnowledgeTreeTwig(int kid) {
        super(kid);
    }

    @Override
    public String getObjectType() {
        return KnowledgeTreeTwig.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeTreeTwig.TYPE;
    }

}
