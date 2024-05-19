package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlockFact {
    public final BlockArray array;
    public MapChunk chunk;
    public final int x;
    public final int y;
    public final int z;
}
