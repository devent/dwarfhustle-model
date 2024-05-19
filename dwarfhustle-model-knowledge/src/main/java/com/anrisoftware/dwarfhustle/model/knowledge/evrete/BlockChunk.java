package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.util.List;

import org.eclipse.collections.api.list.MutableList;

import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlockChunk {

    public final GameChunkPos pos;

    public BlockChunk parent;

    public List<BlockChunk> children;

    public boolean changed;

    public void addChild(BlockChunk chunk) {
        var list = (MutableList<BlockChunk>) children;
        list.add(chunk);
    }
}
