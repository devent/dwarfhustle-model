package com.anrisoftware.dwarfhustle.model.api.vegetations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Cereal grasses, bamboos, the grasses of natural grassland and species
 * cultivated in lawns and pasture.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeGrass extends KnowledgeVegetation {

    public static final String OBJECT_TYPE = KnowledgeTree.class.getSimpleName();

    public static final String TYPE = "Grass";

    public KnowledgeGrass(int kid) {
        super(kid);
    }

    @Override
    public String getObjectType() {
        return KnowledgeGrass.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeGrass.TYPE;
    }

}
