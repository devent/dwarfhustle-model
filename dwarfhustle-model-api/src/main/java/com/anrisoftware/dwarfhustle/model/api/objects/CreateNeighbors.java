package com.anrisoftware.dwarfhustle.model.api.objects;

import java.util.function.Function;

public class CreateNeighbors {

    public void createNeighbors(MapChunk mcRoot, MapChunk rootc, Function<Integer, MapChunk> retriever) {
        var pos = rootc.getPos();
        int xs = (pos.ep.x - pos.x) / 2;
        int ys = (pos.ep.y - pos.y) / 2;
        int zs = (pos.ep.z - pos.z) / 2;
        for (int x = pos.x; x < pos.ep.x; x += xs) {
            for (int y = pos.y; y < pos.ep.y; y += ys) {
                for (int z = pos.z; z < pos.ep.z; z += zs) {
                    var cid = rootc.getChunk(x, y, z, x + xs, y + ys, z + zs);
                    assert cid != 0;
                    var chunk = retriever.apply(cid);
                    if (xs > rootc.chunkSize && ys > rootc.chunkSize && zs > rootc.chunkSize) {
                        createNeighbors(mcRoot, chunk, retriever);
                    }
                    var neighbors = new int[NeighboringDir.values().length];
                    int b;
                    for (NeighboringDir dir : NeighboringDir.values()) {
                        int sx = x + (dir.pos.x * xs);
                        int sy = y + (dir.pos.y * ys);
                        int sz = z + (dir.pos.z * zs);
                        int ex = x + xs + (dir.pos.x * xs);
                        int ey = y + ys + (dir.pos.y * ys);
                        int ez = z + zs + (dir.pos.z * zs);
                        if ((b = mcRoot.findChild(sx, sy, sz, ex, ey, ez, retriever)) != 0) {
                            neighbors[dir.ordinal()] = b;
                        }
                    }
                    chunk.setNeighbors(neighbors);
                }
            }
        }
    }

}
