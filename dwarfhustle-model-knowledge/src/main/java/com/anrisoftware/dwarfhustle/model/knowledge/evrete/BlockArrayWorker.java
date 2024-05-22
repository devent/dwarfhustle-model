package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlockArrayWorker {

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
            if (chunk.changed && chunk.isLeaf()) {
                for (int z = 0; z < chunk.getPos().getSizeZ(); z++) {
                    for (int y = 0; y < chunk.getPos().getSizeY(); y++) {
                        for (int x = 0; x < chunk.getPos().getSizeX(); x++) {
                            session.insert(new BlockFact(chunk, x, y, z));
                        }
                    }
                }
                session.fire();
                session.clear();
            }
        });
        session.close();

    }
}
