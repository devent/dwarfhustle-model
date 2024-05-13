package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;

import lombok.Data;

/**
 * Fact for TerrainCreateRules.
 */
@Data
public class TerrainFact {

    public final long mid;

    public final MapBlock block;

    public final MapBlock[] neighbors;
}
