/*
 * dwarfhustle-gamemap-model - Game map.
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
package com.anrisoftware.dwarfhustle.model.objects;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Retrieves the {@link GameMapObject} from the {@link GameMap}.
 *
 * @author Erwin Müller {@literal <erwin@mullerlpublic.de}
 */
@ToString(callSuper = true)
public class RetrieveObjectsMessage<T extends ObjectResponseMessage> extends Message {

    /**
     *
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @RequiredArgsConstructor
    @ToString(callSuper = true)
    public static class RetrieveObjectsSuccessMessage extends ObjectResponseMessage {

        public final List<GameMapObject> objects;
    }

    /**
     * Asks with an {@link RetrieveObjectsMessage}.
     */
    public static CompletionStage<? extends ObjectResponseMessage> askRetrieveObjects(ActorSystem<Message> a, long gm,
            GameBlockPos pos, Duration timeout) {
        return askRetrieveObjects(a, gm, pos, timeout, NOP_CONSUMER);
    }

    /**
     * Asks with an {@link RetrieveObjectsMessage}.
     */
    public static CompletionStage<? extends ObjectResponseMessage> askRetrieveObjects(ActorSystem<Message> a, long gm,
            GameBlockPos pos, Duration timeout, Consumer<List<GameMapObject>> consumer) {
        return AskPattern.ask(a, replyTo -> new RetrieveObjectsMessage<>(replyTo, gm, pos, consumer), timeout,
                a.scheduler());
    }

    private static final Consumer<List<GameMapObject>> NOP_CONSUMER = go -> {
    };

    /**
     * Reply to {@link ActorRef}.
     */
    @ToString.Exclude
    public final ActorRef<T> replyTo;

    public final long gm;

    public final GameBlockPos pos;

    public final Consumer<List<GameMapObject>> consumer;

    public RetrieveObjectsMessage(ActorRef<T> replyTo, long gm, GameBlockPos pos,
            Consumer<List<GameMapObject>> consumer) {
        this.replyTo = replyTo;
        this.gm = gm;
        this.pos = pos;
        this.consumer = consumer;
    }

    public RetrieveObjectsMessage(ActorRef<T> replyTo, long gm, GameBlockPos pos) {
        this(replyTo, gm, pos, NOP_CONSUMER);
    }
}
