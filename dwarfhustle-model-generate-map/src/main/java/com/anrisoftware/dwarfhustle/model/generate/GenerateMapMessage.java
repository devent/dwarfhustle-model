/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.generate;

import java.util.Map;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to generate game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString
public class GenerateMapMessage extends Message {

    @RequiredArgsConstructor
    @ToString
    public static class GenerateResponseMessage extends Message {

    }

    @RequiredArgsConstructor
    @ToString
    public static class GenerateErrorMessage extends GenerateResponseMessage {

        @ToString.Exclude
        public final GenerateMapMessage om;

        public final Throwable error;
    }

    @RequiredArgsConstructor
    @ToString
    public static class GenerateSuccessMessage extends GenerateResponseMessage {

        @ToString.Exclude
        public final GenerateMapMessage om;
    }

    @RequiredArgsConstructor
    @ToString
    public static class GenerateProgressMessage extends GenerateResponseMessage {

        @ToString.Exclude
        public final GenerateMapMessage om;

        public final int blocksDone;

        public final boolean generateDone;
    }

    public final ActorRef<GenerateResponseMessage> replyTo;

    public final ActorRef<GenerateProgressMessage> progressTo;

    public final WorldMap worldMap;

    public final GameMap gameMap;

    /**
     * Game map properties that are used to generate the map. Z level starts with 0
     * which means the very top of the map.
     * <ul>
     * <li>{@code ground_level_percent} the ground level percent of the total
     * height. Above this level will be air.
     * <li>{@code soil_level_percent} soil level percent of the total height.
     * <li>{@code sedimentary_level_percent} sedimentary stone level percent of the
     * total height.
     * <li>{@code igneous_level_percent} igneous stone level percent of the total
     * height.
     * <li>{@code magma_level_percent} magma level percent of the total height.
     * </ul>
     */
    public final Map<String, Object> p;

    public final int blockSize;

    public int getSize() {
        return gameMap.getSize();
    }
}
