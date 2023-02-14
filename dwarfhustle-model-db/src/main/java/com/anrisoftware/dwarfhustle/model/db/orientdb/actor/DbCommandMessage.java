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

import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to execute a command on the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class DbCommandMessage<T extends Message> extends DbMessage<T> {

    /**
     * Database command success response with return value.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class DbCommandSuccessMessage<T extends DbMessage<?>> extends DbResponseMessage<T> {

        public final Object value;

        public DbCommandSuccessMessage(T om, Object value) {
            super(om);
            this.value = value;
        }

    }

    /**
     * Database command error response with return value.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class DbCommandErrorMessage<T extends DbMessage<?>> extends DbResponseMessage<T> {

        public final Throwable ex;

        public final Object onError;

        public DbCommandErrorMessage(T om, Throwable ex, Object onError) {
            super(om);
            this.ex = ex;
            this.onError = onError;
        }

    }

    public final Function<Throwable, Object> onError;

    public final Function<ODatabaseDocument, Object> command;

    public DbCommandMessage(ActorRef<T> replyTo, Function<Throwable, Object> onError,
            Function<ODatabaseDocument, Object> command) {
        super(replyTo);
        this.onError = onError;
        this.command = command;
    }

}
