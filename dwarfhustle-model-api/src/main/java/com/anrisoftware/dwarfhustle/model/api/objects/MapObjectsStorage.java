/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.api.objects;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Store the object ID and object type for the (x,y,z) map block.
 */
public interface MapObjectsStorage extends AutoCloseable {

    /**
     * Stores the game map object in the (x,y,z) block in the database.
     */
    void putObject(int x, int y, int z, int type, long id);

    /**
     * Mass storage for game map objects.
     */
    void putObjects(List<? extends StoredObject> objects);

    /**
     * Mass storage for game map objects.
     */
    void putObjects(Iterable<GameMapObject> objects);

    /**
     * Retrieves the game map objects on the (x,y,z) block from the database.
     */
    void getObjects(int x, int y, int z, BiConsumer<Integer, Long> consumer);

    /**
     * Retrieves the game map objects from a range start (x,y,z) to end (x,y,z)
     * blocks from the database.
     */
    void getObjectsRange(int sx, int sy, int sz, int ex, int ey, int ez, BiConsumer<Integer, Long> consumer);

}
