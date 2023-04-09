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
package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsSuccessMessage;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to save a {@link GameObject} in the database. Responds with either
 * {@link SaveObjectSuccessMessage} or {@link SaveObjectErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class SaveObjectMessage<T extends Message> extends ObjectsMessage<T> {

    @ToString(callSuper = true)
    public static class SaveObjectSuccessMessage<T extends Message>
            extends ObjectsSuccessMessage<SaveObjectMessage<T>> {

        public final GameObject go;

        public SaveObjectSuccessMessage(SaveObjectMessage<T> om, GameObject go) {
            super(om);
            this.go = go;
        }

    }

    @ToString(callSuper = true)
    public static class SaveObjectErrorMessage<T extends Message> extends ObjectsErrorMessage<SaveObjectMessage<T>> {
        public SaveObjectErrorMessage(SaveObjectMessage<T> om, Throwable error) {
            super(om, error);
        }

    }

    public final GameObject go;

    public SaveObjectMessage(ActorRef<T> replyTo, GameObject go) {
        super(replyTo);
        this.go = go;
    }

}
