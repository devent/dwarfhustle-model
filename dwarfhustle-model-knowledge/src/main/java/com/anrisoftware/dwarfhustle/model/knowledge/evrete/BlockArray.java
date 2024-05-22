package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer.calcIndex;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

/**
 * Sizes:
 * 
 * <pre>
 * 512 * 512 * 128 * ( 
 * + 2 parent
 * + 2 material
 * + 2 object
 * + 4 p
 * + 2 temp
 * + 2 lux
 * )
 * = 448 MB
 * </pre>
 */
public class BlockArray {

    public static void setMaterial(MapChunk chunk, int x, int y, int z, short m) {
        int off = getShortOffset(chunk, x, y, z);
        MapBlockBuffer.setMaterial(chunk.blocks.get(), off, m);
    }

    public static int getMaterial(MapChunk chunk, int x, int y, int z) {
        int off = getShortOffset(chunk, x, y, z);
        return MapBlockBuffer.getMaterial(chunk.blocks.get(), off);
    }

    public static void setProp(MapChunk chunk, int x, int y, int z, int p) {
        int off = getIntOffset(chunk, x, y, z);
        MapBlockBuffer.setProp(chunk.intBuffer.get(), off, p);
    }

    public static int getProp(MapChunk chunk, int x, int y, int z) {
        int off = getIntOffset(chunk, x, y, z);
        return MapBlockBuffer.getProp(chunk.intBuffer.get(), off);
    }

    public boolean isProp(MapChunk chunk, int x, int y, int z, int flags) {
        return (getProp(chunk, x, y, z) & flags) == flags;
    }

    public static void setTemp(MapChunk chunk, int x, int y, int z, short t) {
        int off = getShortOffset(chunk, x, y, z);
        MapBlockBuffer.setTemp(chunk.blocks.get(), off, t);
    }

    public static int getTemp(MapChunk chunk, int x, int y, int z) {
        int off = getShortOffset(chunk, x, y, z);
        return MapBlockBuffer.getTemp(chunk.blocks.get(), off);
    }

    public static void setLux(MapChunk chunk, int x, int y, int z, int l) {
        int off = getShortOffset(chunk, x, y, z);
        MapBlockBuffer.setLux(chunk.blocks.get(), off, l);
    }

    public static int getLux(MapChunk chunk, int x, int y, int z) {
        int off = getShortOffset(chunk, x, y, z);
        return MapBlockBuffer.getTemp(chunk.blocks.get(), off);
    }

    private static int getByteOffset(MapChunk chunk, int x, int y, int z) {
        return calcIndex(chunk.pos.getSizeX(), chunk.pos.getSizeY(), chunk.pos.getSizeZ(), chunk.pos.x, chunk.pos.y,
                chunk.pos.z, x, y, z) * MapBlockBuffer.SIZE;
    }

    private static int getShortOffset(MapChunk chunk, int x, int y, int z) {
        return getByteOffset(chunk, x, y, z) / 2;
    }

    private static int getIntOffset(MapChunk chunk, int x, int y, int z) {
        return getByteOffset(chunk, x, y, z) / 4;
    }

}
