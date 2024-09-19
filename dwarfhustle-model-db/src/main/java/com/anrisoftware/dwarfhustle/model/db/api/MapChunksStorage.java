package com.anrisoftware.dwarfhustle.model.db.api;

import java.util.List;
import java.util.function.Consumer;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

/**
 * Stores the {@link MapChunk}(s) of the map.
 */
public interface MapChunksStorage extends AutoCloseable {

    /**
     * Stores the {@link MapChunk} in the database.
     */
    void putChunk(MapChunk chunk);

    /**
     * Mass storage for {@link MapChunk}(s).
     */
    void putChunks(List<MapChunk> chunks);

    /**
     * Mass storage for {@link MapChunk}(s).
     */
    void putChunks(Iterable<MapChunk> chunks);

    /**
     * Returns the {@link MapChunk} with the chunk ID.
     */
    MapChunk getChunk(int cid);

    /**
     * Retrieves all {@link MapChunk} chunks.
     */
    void forEachValue(Consumer<MapChunk> consumer);

}
