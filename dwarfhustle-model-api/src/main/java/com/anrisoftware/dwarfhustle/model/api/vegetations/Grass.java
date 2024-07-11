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
@EqualsAndHashCode(callSuper = true)
@Data
public class Grass extends Vegetation {

    public static final int OBJECT_TYPE = Grass.class.getSimpleName().hashCode();

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }
}
