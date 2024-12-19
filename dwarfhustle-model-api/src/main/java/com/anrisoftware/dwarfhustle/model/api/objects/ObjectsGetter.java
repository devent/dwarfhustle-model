/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
 * Returns {@link GameObject} game objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface ObjectsGetter {

    /**
     * Exception if there was an error getting the {@link GameObject}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public static class ObjectsGetterException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ObjectsGetterException(String message, Throwable cause) {
            super(message, cause);
        }

        public ObjectsGetterException(String message) {
            super(message);
        }
    }

    static CompletionStage<ObjectsGetter> EMPTY = CompletableFuture.supplyAsync(() -> new ObjectsGetter() {

        @Override
        public <T extends GameObject> T get(int type, long key) throws ObjectsGetterException {
            return null;
        }
    });

    /**
     * Returns the {@link GameObject}.
     */
    <T extends GameObject> T get(int type, long key) throws ObjectsGetterException;
}
