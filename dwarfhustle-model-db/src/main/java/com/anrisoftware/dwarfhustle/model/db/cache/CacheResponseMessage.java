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
package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Base class of cache responses.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString
public class CacheResponseMessage<T extends CacheMessage<?>> extends Message {

    /**
     * Cache error response.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class CacheErrorMessage<T extends CacheMessage<?>> extends CacheResponseMessage<T> {

        public final Throwable error;

        public CacheErrorMessage(T m, Throwable error) {
            super(m);
            this.error = error;
        }
    }

    /**
     * Cache success response.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class CacheSuccessMessage<T extends CacheMessage<?>> extends CacheResponseMessage<T> {
        public CacheSuccessMessage(T m) {
            super(m);
        }
    }

    @ToString.Exclude
    public final T m;

}
