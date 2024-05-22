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
            if (chunk.isLeaf() && chunk.pos.ep.z < 128) {
                chunk.changed = true;
            }
        });
        store.forEachValue(chunk -> {
            if (chunk.changed && chunk.isLeaf()) {
                var pos = chunk.getPos();
                for (int z = pos.z; z < pos.ep.z; z++) {
                    for (int y = pos.y; y < pos.ep.y; y++) {
                        for (int x = pos.x; x < pos.ep.x; x++) {
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
