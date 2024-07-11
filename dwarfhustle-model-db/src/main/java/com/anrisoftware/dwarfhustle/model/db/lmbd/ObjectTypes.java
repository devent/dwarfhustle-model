package com.anrisoftware.dwarfhustle.model.db.lmbd;

import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Grass;

public class ObjectTypes {

    public static final IntSet OBJECT_TYPES;

    static {
        MutableIntSet set = IntSets.mutable.empty();
        set.add(WorldMap.OBJECT_TYPE);
        set.add(GameMap.OBJECT_TYPE);
        set.add(Grass.OBJECT_TYPE);
        OBJECT_TYPES = set;
    }
}
