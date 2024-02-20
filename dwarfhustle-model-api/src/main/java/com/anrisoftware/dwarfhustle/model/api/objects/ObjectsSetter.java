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
 * Stores {@link GameObject} game objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface ObjectsSetter {

    /**
     * Exception if there was an error getting the {@link GameObject}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public static class ObjectsSetterException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ObjectsSetterException(String message, Throwable cause) {
            super(message, cause);
        }

        public ObjectsSetterException(String message) {
            super(message);
        }
    }

    /**
     * Stores the {@link GameObject}.
     */
    <T extends GameObject> void set(Class<T> typeClass, String type, T key) throws ObjectsSetterException;

}
