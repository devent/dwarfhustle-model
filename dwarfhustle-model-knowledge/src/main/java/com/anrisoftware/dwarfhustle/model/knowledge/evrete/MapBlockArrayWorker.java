package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapBlockArrayWorker {

    private final MapChunksStore store;

    private final BlockArrayRules rules;

    public void runRules() {
        var session = rules.mapKn.newStatefulSession();
        store.forEachValue(chunk -> {
            if (chunk.isLeaf() && chunk.pos.ep.z <= 64) {
                chunk.changed = true;
            }
        });
        store.forEachValue(chunk -> {
            if (chunk.isLeaf()) {
                chunk.forEachBlocks((block) -> {
                    session.insert(new MapBlockFact(store::getChunk, chunk, block));
                });
                session.fire();
                session.clear();
            }
        });
        session.close();

    }
}
