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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

    static CompletionStage<ObjectsSetter> EMPTY = CompletableFuture.supplyAsync(() -> new ObjectsSetter() {

        @Override
        public void set(int type, GameObject go) throws ObjectsSetterException {
        }

        @Override
        public void set(int type, Iterable<GameObject> values) throws ObjectsSetterException {
        }
    });

    /**
     * Stores the {@link GameObject}.
     */
    void set(int type, GameObject go) throws ObjectsSetterException;

    /**
     * Bulk store the {@link GameObject}(s).
     */
    void set(int type, Iterable<GameObject> values) throws ObjectsSetterException;

    /**
     * Removes the {@link GameObject}.
     */
    default void remove(int type, GameObject go) throws ObjectsSetterException {
        // nop
    }

    /**
     * Removes the {@link GameObject}.
     */
    default void remove(int type, long id) throws ObjectsSetterException {
        // nop
    }

}
