/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.cache;

import static akka.actor.typed.javadsl.AskPattern.ask;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import lombok.ToString;

/**
 * Message to put an object in the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class CachePutMessage<T extends Message> extends CacheMessage<T> {

    /**
     * Asks the actor to put the value with the key in the cache.
     *
     * @param a       the {@link ActorRef}.
     * @param timeout the {@link Duration} timeout.
     * @param value   the {@link GameObject} value.
     * @return {@link CompletionStage} with the {@link CacheResponseMessage}.
     */
    public static CompletionStage<CacheResponseMessage<?>> askCachePut(ActorRef<Message> a, Scheduler scheduler,
            Duration timeout, GameObject value) {
        return ask(a, replyTo -> new CachePutMessage<>(replyTo, value), timeout, scheduler);
    }

    public final GameObject value;

    public CachePutMessage(ActorRef<T> replyTo, GameObject value) {
        super(replyTo);
        this.value = value;
    }

}
