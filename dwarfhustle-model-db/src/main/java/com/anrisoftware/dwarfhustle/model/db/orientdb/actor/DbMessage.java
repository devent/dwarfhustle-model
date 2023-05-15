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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Base for all database messages.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
@RequiredArgsConstructor
public class DbMessage<T extends DbMessage<?>> extends Message {

    /**
     * Base class of database responses.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public static class DbResponseMessage<T extends DbMessage<?>> extends Message {
    }

    /**
     * Database error response.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @RequiredArgsConstructor
    public static class DbErrorMessage<T extends DbMessage<?>> extends DbResponseMessage<T> {
        public final Exception error;
    }

    /**
     * Database success response.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public static class DbSuccessMessage<T extends DbMessage<?>> extends DbResponseMessage<T> {
    }

    /**
     * Reply to {@link ActorRef}.
     */
    public final ActorRef<T> replyTo;

}
