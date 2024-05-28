package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer.calcIndex;

import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

import lombok.RequiredArgsConstructor;

/**
 * Terrain block fact.
 * <p>
 * 
 */
@RequiredArgsConstructor
public class BlockFact {

    public final MapChunk chunk;

    public final int x;

    public final int y;

    public final int z;

    public final Function<Integer, MapChunk> retriever;

    public void setMaterial(int x, int y, int z, int m) {
        int off = getByteOffset(x, y, z);
        MapBlockBuffer.setMaterial(chunk.getBlocksBuffer(), off, m);
    }

    public void setMaterial(int m) {
        setMaterial(x, y, z, m);
    }

    public int getMaterial(int x, int y, int z) {
        int off = getByteOffset(x, y, z);
        return MapBlockBuffer.getMaterial(chunk.getBlocksBuffer(), off);
    }

    public int getMaterial() {
        return getMaterial(x, y, z);
    }

    public void setObject(int x, int y, int z, int o) {
        int off = getByteOffset(x, y, z);
        MapBlockBuffer.setObject(chunk.getBlocksBuffer(), off, o);
    }

    public void setObject(int o) {
        setObject(x, y, z, o);
    }

    public int getObject(int x, int y, int z) {
        int off = getByteOffset(x, y, z);
        return MapBlockBuffer.getObject(chunk.getBlocksBuffer(), off);
    }

    public int getObject() {
        return getObject(x, y, z);
    }

    public void setProp(int x, int y, int z, int p) {
        int off = getByteOffset(x, y, z);
        MapBlockBuffer.setProp(chunk.getBlocksBuffer(), off, p);
    }

    public void setProp(int p) {
        setProp(x, y, z, p);
    }

    public int getProp(int x, int y, int z) {
        int off = getByteOffset(x, y, z);
        return MapBlockBuffer.getProp(chunk.getBlocksBuffer(), off);
    }

    public int getProp() {
        return getProp(x, y, z);
    }

    public boolean isProp(int x, int y, int z, int flags) {
        return (getProp(x, y, z) & flags) == flags;
    }

    public boolean isProp(int flags) {
        return isProp(x, y, z, flags);
    }

    public void addProp(int x, int y, int z, int flags) {
        int p = getProp(x, y, z);
        setProp(x, y, z, p | flags);
    }

    public void addProp(int flags) {
        addProp(x, y, z, flags);
    }

    public void removeProp(int x, int y, int z, int flags) {
        int p = getProp(x, y, z);
        setProp(x, y, z, p & ~flags);
    }

    public void removeProp(int flags) {
        removeProp(x, y, z, flags);
    }

    public void setTemp(int x, int y, int z, int t) {
        int off = getByteOffset(x, y, z);
        MapBlockBuffer.setTemp(chunk.getBlocksBuffer(), off, t);
    }

    public void setTemp(int t) {
        setTemp(x, y, z, t);
    }

    public int getTemp(int x, int y, int z) {
        int off = getByteOffset(x, y, z);
        return MapBlockBuffer.getTemp(chunk.getBlocksBuffer(), off);
    }

    public int getTemp() {
        return getTemp(x, y, z);
    }

    public void setLux(int x, int y, int z, int l) {
        int off = getByteOffset(x, y, z);
        MapBlockBuffer.setLux(chunk.getBlocksBuffer(), off, l);
    }

    public void setLux(int l) {
        setLux(x, y, z, l);
    }

    public int getLux(int x, int y, int z) {
        int off = getByteOffset(x, y, z);
        return MapBlockBuffer.getTemp(chunk.getBlocksBuffer(), off);
    }

    public int getLux() {
        return getLux(x, y, z);
    }

    private int getByteOffset(int x, int y, int z) {
        return calcIndex(chunk.pos.getSizeX(), chunk.pos.getSizeY(), chunk.pos.getSizeZ(), chunk.pos.x, chunk.pos.y,
                chunk.pos.z, x, y, z) * MapBlockBuffer.SIZE;
    }

}
