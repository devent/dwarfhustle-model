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
@EqualsAndHashCode(callSuper = true)
@Data
public class TreeTrunk extends Tree {

    public static final int OBJECT_TYPE = TreeTrunk.class.getSimpleName().hashCode();

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

}
