package com.anrisoftware.dwarfhustle.model.db.api;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

/**
 * Stores the {@link MapChunk}(s) and {@link MapBlock}(s) of the map.
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
     * Stores the {@link MapBlock} in the database.
     */
    void putBlock(MapChunk chunk, MapBlock block);

    /**
     * Stores the {@link MapBlock} in the database.
     */
    void putBlocks(MapChunk chunk, List<MapBlock> blocks);

    /**
     * Returns the {@link MapBlock} from the database.
     */
    MapBlock getBlock(MapChunk chunk, GameBlockPos pos);

    /**
     * Retrieves all {@link MapChunk} chunks.
     */
    void forEachValue(Consumer<MapChunk> consumer);

    /**
     * Retrieves a {@link MapBlock}'s buffer for the {@link MapChunk}.
     */
    void withBlockBuffer(MapChunk chunk, Consumer<MutableDirectBuffer> consumer);

    /**
     * Retrieves a {@link MapBlock}'s buffer for the {@link MapChunk}.
     */
    <T> T withBlockReadBuffer(MapChunk chunk, Function<DirectBuffer, T> consumer);

}
