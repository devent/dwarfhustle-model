/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Objects response message.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
@RequiredArgsConstructor
public class ObjectsResponseMessage<T extends ObjectsMessage<?>> extends Message {

    /**
     * Objects success response.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class ObjectsSuccessMessage<T extends ObjectsMessage<?>> extends ObjectsResponseMessage<T> {
        public ObjectsSuccessMessage(T om) {
            super(om);
        }

    }

    /**
     * Objects error response.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class ObjectsErrorMessage<T extends ObjectsMessage<?>> extends ObjectsResponseMessage<T> {
        public final Throwable error;

        public ObjectsErrorMessage(T om, Throwable error) {
            super(om);
            this.error = error;
        }

    }

    public final T om;
}
