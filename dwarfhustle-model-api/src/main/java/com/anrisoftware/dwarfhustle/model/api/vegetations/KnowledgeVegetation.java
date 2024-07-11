package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.eclipse.collections.api.set.primitive.IntSet;

import com.anrisoftware.dwarfhustle.model.api.map.ObjectType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Grasses, shrubs, trees.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeVegetation extends ObjectType {

    public static final int OBJECT_TYPE = KnowledgeVegetation.class.getSimpleName().hashCode();

    public static final String TYPE = "Vegetation";

    /**
     * Season(s) of the growing time.
     */
    public IntSet growingSeason;

    /**
     * Growing speed in units/day giving best conditions.
     */
    public int growingSpeed;

    /**
     * Minimum temperature for the plant, below the plant will die.
     */
    public float growingMinTemp;

    /**
     * Maximum temperature for the plant, above the plant will die.
     */
    public float growingMaxTemp;

    /**
     * Optimal temperature for the plant, above or below the plant will receive
     * growth penalties.
     */
    public float growingOptTemp;

    /**
     * Defines where the plant can grow.
     */
    public IntSet growingSoil;

    /**
     * Month(s) of the flowering period.
     */
    public IntSet floweringMonths;

    public KnowledgeVegetation(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return KnowledgeVegetation.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeVegetation.TYPE;
    }

}
