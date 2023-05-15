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

import java.util.function.Consumer;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheSuccessMessage;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to get {@link GameObject} game objects from the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
public class CacheGetMessage<T extends Message> extends CacheMessage<T> {

    /**
     * Message to get {@link GameObject} game objects from the cache.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class CacheGetSuccessMessage<T extends CacheMessage<?>> extends CacheSuccessMessage<T> {
        public final GameObject go;

        public CacheGetSuccessMessage(T m, GameObject go) {
            super(m);
            this.go = go;
        }
    }

    /**
     * Message that the cache does not contain the object.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class CacheGetMissMessage<T extends CacheMessage<?>> extends CacheSuccessMessage<T> {
        public CacheGetMissMessage(T m) {
            super(m);
        }
    }

    private final static Consumer<GameObject> EMPTY_CONSUMER = go -> {
    };

    private final static Runnable EMPTY_ON_MISS = () -> {
    };

    public final Class<? extends GameObject> typeClass;

    public final String type;

    public final Object key;

    public final Consumer<GameObject> consumer;

    public final Runnable onMiss;

    public CacheGetMessage(ActorRef<T> replyTo, Class<? extends GameObject> typeClass, String type, Object key) {
        this(replyTo, typeClass, type, key, EMPTY_CONSUMER, EMPTY_ON_MISS);
    }

    public CacheGetMessage(ActorRef<T> replyTo, Class<? extends GameObject> typeClass, String type, Object key,
            Consumer<GameObject> consumer) {
        this(replyTo, typeClass, type, key, consumer, EMPTY_ON_MISS);
    }

    public CacheGetMessage(ActorRef<T> replyTo, Class<? extends GameObject> typeClass, String type, Object key,
            Consumer<GameObject> consumer, Runnable onMiss) {
        super(replyTo);
        this.typeClass = typeClass;
        this.type = type;
        this.key = key;
        this.consumer = consumer;
        this.onMiss = onMiss;
    }

}
