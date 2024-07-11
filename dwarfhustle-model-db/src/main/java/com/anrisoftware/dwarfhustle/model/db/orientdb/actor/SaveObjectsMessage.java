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

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import lombok.ToString;

/**
 * Message to save multiple {@link StoredObject}s in the database. Responds with
 * either {@link DbSuccessMessage} or {@link DbErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class SaveObjectsMessage<T extends Message> extends DbMessage<T> {

    /**
     * Asks the actor to saves multiple {@link StoredObject}s in the database.
     *
     * @param a       the {@link ActorSystem}.
     * @param timeout the {@link Duration} timeout.
     * @return {@link CompletionStage} with the {@link DbResponseMessage}.
     */
    public static CompletionStage<DbResponseMessage<?>> askSaveObjects(ActorSystem<Message> a, Duration timeout,
            int objectType, Iterable<GameObject> values) {
        return ask(a, replyTo -> new SaveObjectsMessage<>(replyTo, objectType, values), timeout, a.scheduler());
    }

    public final int objectType;

    public final Iterable<GameObject> gos;

    public SaveObjectsMessage(ActorRef<T> replyTo, int objectType, Iterable<GameObject> gos) {
        super(replyTo);
        this.objectType = objectType;
        this.gos = gos;
    }

}
