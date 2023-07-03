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

/**
 * Stores and retrieves the properties of a {@link StoredObject} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface GameObjectStorage {

	/**
     * Stores the {@link StoredObject} properties in the database object.
     *
     * @param db a reference to the database.
     * @param o  a reference to the storage object of the database.
     * @param go the {@link StoredObject} to store.
     */
    void store(Object db, Object o, StoredObject go);

	/**
     * Retrieves the {@link StoredObject} properties from the database object.
     *
     * @param db a reference to the database.
     * @param o  a reference to the storage object of the database.
     * @param go the {@link StoredObject} to set the properties.
     * @return the {@link StoredObject}.
     */
    StoredObject retrieve(Object db, Object o, StoredObject go);

	/**
     * Returns a new {@link StoredObject}.
     */
    StoredObject create();
}
