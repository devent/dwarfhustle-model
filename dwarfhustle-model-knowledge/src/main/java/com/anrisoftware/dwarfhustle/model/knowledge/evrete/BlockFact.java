package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class BlockFact {
    public final MapChunk chunk;
    public final int x;
    public final int y;
    public final int z;
    public final Function<Integer, MapChunk> retriever;
}
