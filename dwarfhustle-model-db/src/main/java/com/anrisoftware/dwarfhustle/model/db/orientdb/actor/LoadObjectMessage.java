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

import static akka.actor.typed.javadsl.AskPattern.ask;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to load a {@link StoredObject} from the database. Responds with
 * either {@link LoadObjectSuccessMessage}, {@link LoadObjectNotFoundMessage} or
 * {@link DbErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
public class LoadObjectMessage<T extends Message> extends DbMessage<T> {

    /**
     * Asks the actor to load a {@link StoredObject} from the database.
     *
     * @param a       the {@link ActorSystem}.
     * @param timeout the {@link Duration} timeout.
     * @return {@link CompletionStage} with the {@link DbResponseMessage}.
     */
    public static CompletionStage<DbResponseMessage<?>> askLoadObject(ActorSystem<Message> a, Duration timeout,
            int objectType, Consumer<StoredObject> consumer, Function<ODatabaseDocument, OResultSet> query) {
        return ask(a, replyTo -> new LoadObjectMessage<>(replyTo, objectType, consumer, query), timeout, a.scheduler());
    }

    /**
     * Asks the actor to load a {@link StoredObject} from the database.
     *
     * @param a       the {@link ActorSystem}.
     * @param timeout the {@link Duration} timeout.
     * @return {@link CompletionStage} with the {@link DbResponseMessage}.
     */
    public static CompletionStage<DbResponseMessage<?>> askLoadObject(ActorSystem<Message> a, Duration timeout,
            int objectType, Function<ODatabaseDocument, OResultSet> query) {
        return ask(a, replyTo -> new LoadObjectMessage<>(replyTo, objectType, query), timeout, a.scheduler());
    }

    @RequiredArgsConstructor
    public static class LoadObjectSuccessMessage<T extends Message> extends DbSuccessMessage<T> {
        public final StoredObject go;
        public final Consumer<StoredObject> consumer;
    }

    @RequiredArgsConstructor
    public static class LoadObjectNotFoundMessage<T extends Message> extends DbSuccessMessage<T> {
    }

    private static final Consumer<StoredObject> EMPTY_CONSUMER = go -> {
    };

    public final int objectType;

    public final Consumer<StoredObject> consumer;

    public final Function<ODatabaseDocument, OResultSet> query;

    public LoadObjectMessage(ActorRef<T> replyTo, int objectType, Function<ODatabaseDocument, OResultSet> query) {
        this(replyTo, objectType, EMPTY_CONSUMER, query);
    }

    public LoadObjectMessage(ActorRef<T> replyTo, int objectType, Consumer<StoredObject> consumer,
            Function<ODatabaseDocument, OResultSet> query) {
        super(replyTo);
        this.objectType = objectType;
        this.consumer = consumer;
        this.query = query;
    }

}
