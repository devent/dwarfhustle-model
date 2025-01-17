/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.db.api;

import java.util.List;
import java.util.function.Consumer;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;

/**
 * Stores the {@link MapChunk}(s) of the map.
 */
public interface MapChunksStorage extends AutoCloseable, ObjectsSetter {

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
    MapChunk getChunk(long cid);

    /**
     * Retrieves all {@link MapChunk} chunks.
     */
    void forEachValue(Consumer<MapChunk> consumer);

}
