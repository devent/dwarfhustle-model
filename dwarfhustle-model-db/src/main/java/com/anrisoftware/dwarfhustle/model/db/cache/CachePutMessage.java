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
package com.anrisoftware.dwarfhustle.model.db.cache;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
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
     * @param a       the {@link ActorSystem}.
     * @param key     the {@link Object} key.
     * @param value   the {@link GameObject} value.
     * @param timeout the {@link Duration} timeout.
     * @return {@link CompletionStage} with the {@link CacheResponseMessage}.
     */
    public static CompletionStage<CacheResponseMessage<?>> askCachePut(ActorSystem<Message> a, Object key,
            GameObject value, Duration timeout) {
        return AskPattern.ask(a, replyTo -> new CachePutMessage<>(replyTo, key, value), timeout, a.scheduler());
    }

    public final Object key;

    public final GameObject value;

    public CachePutMessage(ActorRef<T> replyTo, Object key, GameObject value) {
        super(replyTo);
        this.key = key;
        this.value = value;
    }

}
