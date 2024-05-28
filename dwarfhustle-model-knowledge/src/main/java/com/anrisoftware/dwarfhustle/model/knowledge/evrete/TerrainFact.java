package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

/**
 * Terrain block fact with the terrain material.
 * <p>
 * 
 */
public class TerrainFact extends BlockFact {

    public final int[][][] terrain;

    public TerrainFact(MapChunk chunk, int x, int y, int z, Function<Integer, MapChunk> retriever, int[][][] terrain) {
        super(chunk, x, y, z, retriever);
        this.terrain = terrain;
    }

    public int getTerrain(int x, int y, int z) {
        return terrain[z][y][x];
    }

    public int getTerrain() {
        return getTerrain(x, y, z);
    }
}
