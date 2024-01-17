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
 * Message to import the {@link TerrainLoadImage} in the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
@RequiredArgsConstructor
public class ImportImageMessage<T extends Message> extends Message {

    /**
     * Message that the import finished successfully.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    @RequiredArgsConstructor
    public static class ImportImageSuccessMessage<T extends Message> extends Message {
    }

    /**
     * Asks the actor to import the {@link TerrainLoadImage} in the database.
     *
     * @param a       the {@link ActorSystem}.
     * @param timeout the {@link Duration} timeout.
     * @return {@link CompletionStage} with the {@link Message}.
     */
    public static CompletionStage<Message> askImportImage(ActorSystem<Message> a, Duration timeout, URL url,
            TerrainLoadImage image, long mapid) {
        return AskPattern.ask(a, replyTo -> new ImportImageMessage<>(replyTo, url, image, mapid), timeout,
                a.scheduler());
    }

    /**
     * Reply to {@link ActorRef}.
     */
    public final ActorRef<T> replyTo;

    public final URL url;

    public final TerrainLoadImage image;

    public final long mapid;

}
