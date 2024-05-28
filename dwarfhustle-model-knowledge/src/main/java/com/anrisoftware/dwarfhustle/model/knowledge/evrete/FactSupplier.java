package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

@FunctionalInterface
public interface FactSupplier<T extends BlockFact> {

    T create(MapChunk chunk, int x, int y, int z);
}
