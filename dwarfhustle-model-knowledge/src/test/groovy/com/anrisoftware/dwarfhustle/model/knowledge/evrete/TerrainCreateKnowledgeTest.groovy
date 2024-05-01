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

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.*
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeGetMessage.askKnowledgeObjects
import static java.time.Duration.ofSeconds
import static java.util.concurrent.CompletableFuture.supplyAsync
import static org.junit.jupiter.params.provider.Arguments.of

import java.util.stream.Stream

import org.evrete.api.RuleSession
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
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

    static RuleSession session

    static long OXYGEN_ID = 908

    static long STONE_ID = 837

    static long LIQUID_ID = 915

    static TILE_RAMP_TRI = 825
    static TILE_RAMP_SINGLE =  824
    static TILE_RAMP_PERP = 823
    static TILE_RAMP_EDGE_OUT = 822
    static TILE_RAMP_EDGE_IN =  821
    static TILE_RAMP_CORNER  =  820
    static TILE_BLOCK = 819

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
        this.knowledge = new TerrainCreateKnowledge({ timeout, typeClass, type -> askKnowledgeObjects(actor.actorSystem, timeout, typeClass, type) })
        this.session = knowledge.createSession()
    }

    static Stream ruleset_declaration() {
        MapBlock block
        MapBlock[] neighbors
        def args = []

        // block with material 0 is gas oxygen empty block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        args << of(0, block, neighbors, OXYGEN_ID, TILE_BLOCK, VISIBLE | EMPTY)

        // block with material oxygen is gas oxygen empty block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        args << of(OXYGEN_ID, block, neighbors, OXYGEN_ID, TILE_BLOCK, VISIBLE | EMPTY)

        // block with material water no neighbors is visible block liquid
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        args << of(LIQUID_ID, block, neighbors, LIQUID_ID, TILE_BLOCK, VISIBLE | LIQUID)

        // block with material stone no neighbors is filled visible ramp-single
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        args << of(STONE_ID, block, neighbors, STONE_ID, TILE_RAMP_SINGLE, VISIBLE | FILLED | RAMP)

        // block with material stone with all neighbors is filled visible block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        NeighboringDir.DIRS_SAME_LEVEL.each {
            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), STONE_ID, VISIBLE | FILLED)
        }
        args << of(STONE_ID, block, neighbors, STONE_ID, TILE_BLOCK, VISIBLE | FILLED)

        // block with material stone with filled U neighbor is hidden block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        NeighboringDir.DIRS_SAME_LEVEL.each {
            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), STONE_ID, VISIBLE | FILLED)
        }
        neighbors[NeighboringDir.U.ordinal()] = createBlock(1, block.pos.add(NeighboringDir.U.pos), STONE_ID, FILLED)
        args << of(STONE_ID, block, neighbors, STONE_ID, TILE_BLOCK, HIDDEN | FILLED)

        // block with material stone with empty U neighbor is visible block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        NeighboringDir.DIRS_SAME_LEVEL.each {
            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), STONE_ID, VISIBLE | FILLED)
        }
        neighbors[NeighboringDir.U.ordinal()] = createBlock(1, block.pos.add(NeighboringDir.U.pos), OXYGEN_ID, EMPTY)
        args << of(STONE_ID, block, neighbors, STONE_ID, TILE_BLOCK, VISIBLE | FILLED)

        // block with material stone with liquid U neighbor is visible block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        NeighboringDir.DIRS_SAME_LEVEL.each {
            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), STONE_ID, VISIBLE | FILLED)
        }
        neighbors[NeighboringDir.U.ordinal()] = createBlock(1, block.pos.add(NeighboringDir.U.pos), LIQUID_ID, LIQUID)
        args << of(STONE_ID, block, neighbors, STONE_ID, TILE_BLOCK, VISIBLE | FILLED)

        // block with material stone is filled one perpendicular neighbor empty is visible ramp-nesw
        //        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        //        neighbors = new MapBlock[NeighboringDir.values().length]
        //        NeighboringDir.DIRS_SAME_LEVEL.each {
        //            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), 844, 0b10)
        //        }
        //        neighbors[NeighboringDir.N.ordinal()] = createBlock(1, block.pos.add(NeighboringDir.N.pos), 905, 0b100)
        //        args << of(844, block, neighbors, 844, 821, 0b00000011)
        //
        //        // block with material stone is filled one edge neighbor empty is visible ramp-edge
        //        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        //        neighbors = new MapBlock[NeighboringDir.values().length]
        //        NeighboringDir.DIRS_SAME_LEVEL.each {
        //            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), 844, 0b10)
        //        }
        //        neighbors[NeighboringDir.NE.ordinal()] = createBlock(1, block.pos.add(NeighboringDir.N.pos), 905, 0b100)
        //        args << of(844, block, neighbors, 844, 822, 0b00000011)
        //
        //        // block with material stone is filled one edge and one perpendicular neighbor empty is visible ramp-edge
        //        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        //        neighbors = new MapBlock[NeighboringDir.values().length]
        //        NeighboringDir.DIRS_SAME_LEVEL.each {
        //            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), 844, 0b10)
        //        }
        //        neighbors[NeighboringDir.N.ordinal()] = createBlock(1, block.pos.add(NeighboringDir.N.pos), 905, 0b100)
        //        neighbors[NeighboringDir.NE.ordinal()] = createBlock(1, block.pos.add(NeighboringDir.N.pos), 905, 0b100)
        //        args << of(844, block, neighbors, 844, 822, 0b00000011)
        //
        //        // block with material stone is filled all neighbors empty is visible ramp-single
        //        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        //        neighbors = new MapBlock[NeighboringDir.values().length]
        //        NeighboringDir.DIRS_SAME_LEVEL.each {
        //            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), 844, 0b00000101)
        //        }
        //        args << of(844, block, neighbors, 844, 820, 0b00000011)

        Stream.of(args as Object[])
    }

    static MapBlock createBlock(int cid, GameBlockPos pos, long mid, int p) {
        def block = new MapBlock(cid, pos)
        block.setMaterialRid(mid)
        block.setProperties(p)
        return block
    }

    @ParameterizedTest
    @MethodSource
    void ruleset_declaration(long mid, MapBlock block, MapBlock[] neighbors, long eM, long eO, int eP) {
        log.debug "[ruleset_declaration] insert"
        session.insertAndFire(new TerrainFact(mid, block, neighbors))
        log.debug "[ruleset_declaration] done {}", block
        assert block.materialRid == eM
        assert block.objectRid == eO
        assert block.p.bits == eP
    }

    @Test
    void insert_facts_ruleset_declaration() {
        log.debug "[insert_facts_ruleset_declaration] insert"
        for (def args : ruleset_declaration()) {
            def a = args.get()
            session.insert(new TerrainFact(a[0], a[1], a[2]))
        }
        session.fire()
        log.debug "[insert_facts_ruleset_declaration] done"
        session.forEachFact(TerrainFact, { f -> println f })
    }
}
