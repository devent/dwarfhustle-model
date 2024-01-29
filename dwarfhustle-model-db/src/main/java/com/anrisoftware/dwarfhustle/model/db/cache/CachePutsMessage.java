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

import static akka.actor.typed.javadsl.AskPattern.ask;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import lombok.ToString;

/**
 * Message to put multiple objects in the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class CachePutsMessage<T extends Message> extends CacheMessage<T> {

    public static CompletionStage<CacheResponseMessage<?>> askCachePuts(ActorSystem<Message> a, Duration timeout,
            Class<?> keyType, Function<GameObject, Object> key, Iterable<? extends GameObject> values) {
        return ask(a, replyTo -> new CachePutsMessage<>(replyTo, keyType, key, values), timeout, a.scheduler());
    }

    public final Class<?> keyType;

    public final Function<GameObject, Object> key;

    public final Iterable<? extends GameObject> values;

    public CachePutsMessage(ActorRef<T> replyTo, Class<?> keyType, Function<GameObject, Object> key,
            Iterable<? extends GameObject> values) {
        super(replyTo);
        this.keyType = keyType;
        this.key = key;
        this.values = values;
    }

}
