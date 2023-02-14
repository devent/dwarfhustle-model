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
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsSuccessMessage;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to create the schemas and indexes for a new database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
public class CreateSchemasMessage<T extends Message> extends ObjectsMessage<T> {

    public static class CreatedSchemasSuccessMessage<T extends Message>
            extends ObjectsSuccessMessage<CreateSchemasMessage<T>> {
        public CreatedSchemasSuccessMessage(CreateSchemasMessage<T> om) {
            super(om);
        }

    }

    public static class CreatedSchemasErrorMessage<T extends Message>
            extends ObjectsErrorMessage<CreateSchemasMessage<T>> {
        public CreatedSchemasErrorMessage(CreateSchemasMessage<T> om, Throwable error) {
            super(om, error);
        }

    }

    public CreateSchemasMessage(ActorRef<T> replyTo) {
        super(replyTo);
    }

}
