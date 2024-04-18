/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.evrete

import static java.time.Duration.ofSeconds
import static java.util.concurrent.CompletableFuture.supplyAsync
import static org.junit.jupiter.params.provider.Arguments.of

import java.util.stream.Stream

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DwarfhustlePowerloomModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.typed.ActorRef
import groovy.util.logging.Slf4j

/**
 * @see TerrainCreateKnowledge
 */
@Slf4j
class TerrainCreateKnowledgeTest {

    static Injector injector

    static ActorSystemProvider actor

    static ActorRef<Message> knowledgeActor

    static ActorRef<Message> cacheActor

    static TerrainCreateKnowledge knowledge

    @BeforeAll
    static void setUp() {
        injector = Guice.createInjector(new DwarfhustleModelActorsModule(), new DwarfhustlePowerloomModule(), new DwarfhustleModelApiObjectsModule())
        actor = injector.getInstance(ActorSystemProvider.class)
        KnowledgeJcsCacheActor.create(injector, ofSeconds(1), actor.getObjectGetterAsync(PowerLoomKnowledgeActor.ID)).whenComplete({ it, ex ->
            cacheActor = it
        } ).get()
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1), supplyAsync({cacheActor})).whenComplete({ it, ex ->
            knowledgeActor = it
        } ).get()
        this.knowledge = new TerrainCreateKnowledge(actor.actorSystem)
    }

    static Stream ruleset_declaration() {
        MapBlock block
        MapBlock[] neighbors
        def args = []

        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        neighbors[NeighboringDir.N.ordinal()] = new MapBlock(1, new GameBlockPos(10, 9, 0))
        args << of(0, block, neighbors, 3882650435585l, 2)

        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        neighbors[NeighboringDir.N.ordinal()] = new MapBlock(1, new GameBlockPos(10, 9, 0))
        args << of(843, block, neighbors, 3620657430529l, 1)

        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        neighbors[NeighboringDir.N.ordinal()] = new MapBlock(1, new GameBlockPos(10, 9, 0))
        args << of(904, block, neighbors, 3882650435585l, 2)

        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource
    void ruleset_declaration(long mid, MapBlock block, MapBlock[] neighbors, long eM, int eP) {
        def session = knowledge.createSession()
        session.insertAndFire(mid, block, neighbors)
        println block
        assert block.material == eM
        assert block.p.bits == eP
    }
}
