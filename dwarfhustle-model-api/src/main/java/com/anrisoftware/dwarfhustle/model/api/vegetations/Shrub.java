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
@EqualsAndHashCode(callSuper = true)
@Data
public class Shrub extends Vegetation {

    public static final int OBJECT_TYPE = Shrub.class.getSimpleName().hashCode();

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }
}
