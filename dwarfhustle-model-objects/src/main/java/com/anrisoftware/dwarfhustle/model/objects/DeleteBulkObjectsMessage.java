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

import org.eclipse.collections.api.factory.primitive.LongLists;
import org.eclipse.collections.api.list.primitive.LongList;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Removes the {@link GameMapObject}(s) from the block on the {@link GameMap}.
 *
 * @author Erwin Müller {@literal <erwin@mullerlpublic.de}
 */
@ToString(callSuper = true)
public class DeleteBulkObjectsMessage<T extends ObjectResponseMessage> extends Message {

    /**
     *
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @RequiredArgsConstructor
    @ToString(callSuper = true)
    public static class DeleteBulkObjectsSuccessMessage extends ObjectResponseMessage {
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

    public final LongList ids;

    public final Runnable onDeleted;

    public DeleteBulkObjectsMessage(ActorRef<T> replyTo, long gm, int type, Iterable<Long> ids, Runnable onDeleted) {
        this(replyTo, gm, type, LongLists.immutable.withAll(ids), onDeleted);
    }

    public DeleteBulkObjectsMessage(ActorRef<T> replyTo, long gm, int type, Iterable<Long> ids) {
        this(replyTo, gm, type, LongLists.immutable.withAll(ids));
    }

    public DeleteBulkObjectsMessage(ActorRef<T> replyTo, long gm, int type, LongList ids, Runnable onDeleted) {
        this.replyTo = replyTo;
        this.gm = gm;
        this.type = type;
        this.ids = ids;
        this.onDeleted = onDeleted;
    }

    public DeleteBulkObjectsMessage(ActorRef<T> replyTo, long gm, int type, LongList ids) {
        this(replyTo, gm, type, ids, NOP);
    }

}
