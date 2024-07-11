package com.anrisoftware.dwarfhustle.model.api.vegetations;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Grasses, shrubs, trees.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Vegetation extends GameMapObject {

    /**
     * {@link KnowledgeVegetation} RID.
     */
    public int kid;

    /**
     * Status of growth from 0.0 (seedling) to 1.0 (adult plant).
     */
    public float growth;
}
