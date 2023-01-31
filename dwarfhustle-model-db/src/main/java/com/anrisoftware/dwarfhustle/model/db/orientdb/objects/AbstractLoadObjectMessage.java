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

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to load a {@link GameObject} from the database. Responds with either
 * {@link LoadObjectSuccessMessage} or {@link LoadObjectErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class AbstractLoadObjectMessage extends AbstractObjectsReplyMessage {

	@ToString(callSuper = true)
	public static class LoadObjectSuccessMessage extends ObjectsSuccessMessage {

		public final GameObject go;

		public LoadObjectSuccessMessage(AbstractLoadObjectMessage om, GameObject go) {
			super(om);
			this.go = go;
		}

	}

	@ToString(callSuper = true)
	public static class LoadObjectErrorMessage extends ObjectsErrorMessage {
		public LoadObjectErrorMessage(AbstractLoadObjectMessage om, Throwable error) {
			super(om, error);
		}

	}

	public AbstractLoadObjectMessage(ActorRef<ObjectsResponseMessage> replyTo) {
		super(replyTo);
	}

}
