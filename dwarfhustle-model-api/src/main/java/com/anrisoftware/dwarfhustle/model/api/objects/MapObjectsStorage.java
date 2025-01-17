/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.objects;

import org.eclipse.collections.api.LongIterable;

/**
 * Store the object ID and object type for the (x,y,z) map block.
 */
public interface MapObjectsStorage extends AutoCloseable {

    /**
     * Stores the game map object in the (x,y,z) block in the database.
     */
    void putObject(int x, int y, int z, int cid, int type, long id);

    /**
     * Stores the game map object in the (x,y,z) block in the database.
     */
    default void putObject(int cid, GameMapObject o) {
        putObject(o.getPos().getX(), o.getPos().getY(), o.getPos().getZ(), cid, o.getObjectType(), o.getId());
    }

    /**
     * Mass storage for game map objects.
     */
    void putObjects(int cid, int index, int type, LongIterable ids);

    /**
     * Retrieves the game map objects on the (x,y,z) block from the database.
     */
    void getObjects(int x, int y, int z, ObjectsConsumer consumer);

    /**
     * Retrieves the game map objects from a range start (x,y,z) to end (x,y,z)
     * blocks from the database.
     */
    void getObjectsRange(int sx, int sy, int sz, int ex, int ey, int ez, ObjectsConsumer consumer);

    /**
     * Deletes the game map object in the (x,y,z) block with the type and ID in the
     * database.
     */
    void removeObject(int x, int y, int z, int cid, int type, long id);

    /**
     * Deletes the game map object in the (x,y,z) block with the type and ID in the
     * database.
     */
    default void removeObject(int cid, GameMapObject o) {
        removeObject(o.getPos().getX(), o.getPos().getY(), o.getPos().getZ(), cid, o.getObjectType(), o.getId());
    }

}
