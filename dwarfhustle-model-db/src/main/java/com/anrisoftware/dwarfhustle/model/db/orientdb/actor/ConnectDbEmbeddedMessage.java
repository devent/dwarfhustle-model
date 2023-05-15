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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import com.orientechnologies.orient.server.OServer;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to connect to an embedded OrientDb database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
public class ConnectDbEmbeddedMessage<T extends DbMessage<T>> extends DbMessage<T> {

	public final OServer server;

	public final String database;

	public final String user;

	public final String password;

    public ConnectDbEmbeddedMessage(ActorRef<T> replyTo, OServer server, String database, String user,
            String password) {
        super(replyTo);
        this.server = server;
        this.database = database;
        this.user = user;
        this.password = password;
    }

}
