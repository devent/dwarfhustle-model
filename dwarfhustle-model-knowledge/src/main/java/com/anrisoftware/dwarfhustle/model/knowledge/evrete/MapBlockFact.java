package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapBlockFact {
    public final Function<Integer, MapChunk> retriever;
    public final MapChunk chunk;
    public final MapBlock block;
}
