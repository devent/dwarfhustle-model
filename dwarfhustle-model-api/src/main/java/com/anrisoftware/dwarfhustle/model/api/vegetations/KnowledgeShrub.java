package com.anrisoftware.dwarfhustle.model.api.vegetations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Small to medium sized plants.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeShrub extends KnowledgeVegetation {

    public static final String OBJECT_TYPE = KnowledgeTree.class.getSimpleName();

    public static final String TYPE = "Shrub";

    public KnowledgeShrub(int kid) {
        super(kid);
    }

    @Override
    public String getObjectType() {
        return KnowledgeShrub.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeShrub.TYPE;
    }

}
