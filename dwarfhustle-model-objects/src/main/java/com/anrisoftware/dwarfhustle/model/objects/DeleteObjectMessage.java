/*
 * dwarfhustle-model-objects - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Removes the {@link GameMapObject} into the {@link GameMap}.
 *
 * @author Erwin Müller {@literal <erwin@mullerlpublic.de}
 */
@ToString(callSuper = true)
public class DeleteObjectMessage<T extends ObjectResponseMessage> extends Message {

    /**
     *
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @RequiredArgsConstructor
    @ToString(callSuper = true)
    public static class DeleteObjectSuccessMessage extends ObjectResponseMessage {
    }

    private final static Runnable NOP = () -> {
    };

    /**
     * Reply to {@link ActorRef}.
     */
    @ToString.Exclude
    public final ActorRef<T> replyTo;

    public final long gm;

    public final int type;

    public final long id;

    public final Runnable onDeleted;

    public DeleteObjectMessage(ActorRef<T> replyTo, long gm, int type, long id, Runnable onDeleted) {
        this.replyTo = replyTo;
        this.gm = gm;
        this.type = type;
        this.id = id;
        this.onDeleted = onDeleted;
    }

    public DeleteObjectMessage(ActorRef<T> replyTo, long gm, int type, long id) {
        this(replyTo, gm, type, id, NOP);
    }

}
