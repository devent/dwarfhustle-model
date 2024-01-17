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

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import lombok.ToString;

/**
 * Message to connect to a remote OrientDb database. Returns with a
 * {@link ConnectDbSuccessMessage} on success and with a {@link DbErrorMessage}
 * on error.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
public class ConnectDbRemoteMessage<T extends Message> extends DbMessage<T> {

    /**
     * Asks the actor to connect to an remote OrientDb database.
     *
     * @param a       the {@link ActorSystem}.
     * @param timeout the {@link Duration} timeout.
     * @return {@link CompletionStage} with the {@link DbMessage}.
     */
    public static CompletionStage<DbMessage<?>> askConnectDbEmbedded(ActorSystem<Message> a, String url,
            String database, String user, String password, Duration timeout) {
        return AskPattern.ask(a, replyTo -> new ConnectDbRemoteMessage<>(replyTo, url, database, user, password),
                timeout, a.scheduler());
    }

    public final String url;

    public final String database;

    public final String user;

    public final String password;

    public ConnectDbRemoteMessage(ActorRef<T> replyTo, String url, String database, String user, String password) {
        super(replyTo);
        this.url = url;
        this.database = database;
        this.user = user;
        this.password = password;
    }

}
