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
@EqualsAndHashCode(callSuper = true)
@Data
public class Tree extends Vegetation {

    public static final int OBJECT_TYPE = Tree.class.getSimpleName().hashCode();

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

}
