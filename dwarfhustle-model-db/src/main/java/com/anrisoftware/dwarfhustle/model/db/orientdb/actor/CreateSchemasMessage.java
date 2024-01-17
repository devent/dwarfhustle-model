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

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import lombok.ToString;

/**
 * Message to create the schemas and indexes for a new database. Replies with
 * either the {@link CreateSchemasSuccessMessage} or {@link DbErrorMessage}
 * message.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
public class CreateSchemasMessage<T extends Message> extends DbMessage<T> {

    /**
     * Message that the embedded OrientDb server was started successfully.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class CreateSchemasSuccessMessage<T extends Message> extends DbResponseMessage<T> {
    }

    /**
     * Asks the actor to create the schemas and indexes for a new database.
     *
     * @param a       the {@link ActorSystem}.
     * @param timeout the {@link Duration} timeout.
     * @return {@link CompletionStage} with the {@link DbMessage}.
     */
    public static CompletionStage<DbMessage<?>> askCreateSchemas(ActorSystem<Message> a, Duration timeout) {
        return AskPattern.ask(a, replyTo -> new CreateSchemasMessage<>(replyTo), timeout, a.scheduler());
    }

    public CreateSchemasMessage(ActorRef<T> replyTo) {
        super(replyTo);
    }

}
