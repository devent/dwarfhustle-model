/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.terrainimage;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to start an embedded OrientDb server.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
@RequiredArgsConstructor
public class ImporterStartEmbeddedServerMessage<T extends Message> extends Message {

    /**
     * Message that the embedded OrientDb server was started successfully.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    @RequiredArgsConstructor
    public static class ImporterStartEmbeddedServerSuccessMessage<T extends Message> extends Message {
    }

    /**
     * Asks the actor to put the value with the key in the cache.
     *
     * @param a       the {@link ActorSystem}.
     * @param timeout the {@link Duration} timeout.
     * @return {@link CompletionStage} with the {@link Message}.
     */
    public static CompletionStage<Message> askImporterStartEmbeddedServer(ActorSystem<Message> a, Duration timeout, String root,
            URL config, String database, String user, String password) {
        return AskPattern.ask(a,
                replyTo -> new ImporterStartEmbeddedServerMessage<>(replyTo, root, config, database, user, password),
                timeout, a.scheduler());
    }

    /**
     * Reply to {@link ActorRef}.
     */
    public final ActorRef<T> replyTo;

    public final String root;

    public final URL config;

    public final String database;

    public final String user;

    public final String password;

}
