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
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.*
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

    static long OXYGEN_ID = 923

    static long STONE_ID = 852

    static long LIQUID_ID = 930

    static long TILE_RAMP_TRI_S = 840
    static long TILE_RAMP_TRI_W = 839
    static long TILE_RAMP_TRI_E = 838
    static long TILE_RAMP_TRI_N = 837
    static long TILE_RAMP_SINGLE    = 836
    static long TILE_RAMP_PERP_W    = 835
    static long TILE_RAMP_PERP_S    = 834
    static long TILE_RAMP_PERP_E    = 833
    static long TILE_RAMP_PERP_N    = 832
    static long TILE_RAMP_EDGE_OUT_SW = 831
    static long TILE_RAMP_EDGE_OUT_SE = 830
    static long TILE_RAMP_EDGE_OUT_NW = 829
    static long TILE_RAMP_EDGE_OUT_NE = 828
    static long TILE_RAMP_EDGE_IN_SW = 827
    static long TILE_RAMP_EDGE_IN_SE = 826
    static long TILE_RAMP_EDGE_IN_NW = 825
    static long TILE_RAMP_EDGE_IN_NE = 824
    static long TILE_RAMP_CORNER_SW = 823
    static long TILE_RAMP_CORNER_SE = 822
    static long TILE_RAMP_CORNER_NW = 821
    static long TILE_RAMP_CORNER_NE = 820
    static long TILE_BLOCK  = 819

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
        MapBlock[] upBlocks = []
        def args = []

        // block with material 0 is gas oxygen empty block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        args << of(0, block, neighbors, upBlocks, OXYGEN_ID, TILE_BLOCK, DISCOVERED | VISIBLE | EMPTY)

        // block with material oxygen is gas oxygen empty block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        args << of(OXYGEN_ID, block, neighbors, upBlocks, OXYGEN_ID, TILE_BLOCK, DISCOVERED | VISIBLE | EMPTY)

        // block with material water no neighbors is visible block liquid
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        args << of(LIQUID_ID, block, neighbors, upBlocks, LIQUID_ID, TILE_BLOCK, DISCOVERED | VISIBLE | LIQUID)

        // block with material stone no neighbors is filled visible ramp-single
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_SINGLE, DISCOVERED | VISIBLE | FILLED | RAMP)

        // 5. block with material stone with all neighbors is filled visible block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block, DIRS_SAME_LEVEL)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_BLOCK, DISCOVERED | VISIBLE | FILLED)

        // block with material stone with filled U neighbor is hidden block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block, DIRS_SAME_LEVEL)
        neighbors[U.ordinal()] = createBlock(1, block.pos.add(U.pos), STONE_ID, FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_BLOCK, DISCOVERED |HIDDEN | FILLED)

        // block with material stone with empty U neighbor is visible block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block, DIRS_SAME_LEVEL)
        neighbors[U.ordinal()] = createBlock(1, block.pos.add(U.pos), OXYGEN_ID, EMPTY)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_BLOCK, DISCOVERED | VISIBLE | FILLED)

        // block with material stone with liquid U neighbor is visible block
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block, DIRS_SAME_LEVEL)
        neighbors[U.ordinal()] = createBlock(1, block.pos.add(U.pos), LIQUID_ID, LIQUID)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_BLOCK, DISCOVERED | VISIBLE | FILLED)

        // block with material stone with empty E,S,W neighbor and filled N neighbor is visible ramp-tri-s
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_TRI_S, DISCOVERED | VISIBLE | FILLED | RAMP)

        // 10. block with material stone with empty N,S,W neighbor and filled E neighbor is visible ramp-tri-w
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, N, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_TRI_W, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty N,E,W neighbor and filled S neighbor is visible ramp-tri-n
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, N, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_TRI_N, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty N,E,S neighbor and filled W neighbor is visible ramp-tri-e
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, N, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_TRI_E, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty N,SE,W neighbor and filled E,S neighbor is visible ramp-corner-nw
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, N, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, SE, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_CORNER_NW, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty N,E,SW neighbor and filled S,W neighbor is visible ramp-corner-ne
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, N, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, SW, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_CORNER_NE, DISCOVERED | VISIBLE | FILLED | RAMP)

        // 15. block with material stone with empty NW,S,W neighbor and filled N,E neighbor is visible ramp-corner-sw
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, NW, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_CORNER_SW, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty E,S,NW neighbor and filled N,W neighbor is visible ramp-corner-se
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, NW, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_CORNER_SE, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty S neighbor and filled N neighbor and ramp E,W is visible ramp-perp-s
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_PERP_S, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty E neighbor and filled W neighbor and ramp N,S is visible ramp-perp-e
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_PERP_E, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty N neighbor and filled S neighbor and ramp E,W is visible ramp-perp-s
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, N, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_PERP_N, DISCOVERED | VISIBLE | FILLED | RAMP)

        // 20. block with material stone with empty W neighbor and filled E neighbor and ramp N,S is visible ramp-perp-w
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_PERP_W, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty E,S neighbor and filled NW neighbor and ramp N,W is visible ramp-edge-out-se
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, NW, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_EDGE_OUT_SE, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty N,W neighbor and filled SE neighbor and ramp E,S is visible ramp-edge-out-nw
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, N, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, SE, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_EDGE_OUT_NW, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty S,W neighbor and filled NE neighbor and ramp N,E is visible ramp-edge-out-sw
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, NE, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_EDGE_OUT_SW, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty N,E neighbor and filled SW neighbor and ramp S,W is visible ramp-edge-out-ne
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, N, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, SW, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_EDGE_OUT_NE, DISCOVERED | VISIBLE | FILLED | RAMP)

        // 25. block with material stone with empty E neighbor and filled N,W,NW neighbor and ramp S,SW is visible tile-ramp-edge-in-se
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, NW, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, SW, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_EDGE_IN_SE, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty W neighbor and filled E,SE,S neighbor and ramp N,NE is visible tile-ramp-edge-in-nw
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, W, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, SE, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, NE, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_EDGE_IN_NW, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty S neighbor and filled N,NE,E neighbor and ramp W,NW is visible tile-ramp-edge-in-sw
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, S, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, NE, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, E, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, NW, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_EDGE_IN_SW, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone with empty E neighbor and filled S,SW,W neighbor and ramp NW,N is visible tile-ramp-edge-in-ne
        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = createNeighbors(block)
        createBlockNeighbor(block, neighbors, 1, E, OXYGEN_ID, VISIBLE | EMPTY)
        createBlockNeighbor(block, neighbors, 1, S, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, SW, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, W, STONE_ID, VISIBLE | FILLED)
        createBlockNeighbor(block, neighbors, 1, NW, STONE_ID, VISIBLE | FILLED | RAMP)
        createBlockNeighbor(block, neighbors, 1, N, STONE_ID, VISIBLE | FILLED | RAMP)
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_RAMP_EDGE_IN_NE, DISCOVERED | VISIBLE | FILLED | RAMP)

        // block with material stone on map edge with fill neighbor is visible block
        block = new MapBlock(1, new GameBlockPos(0, 0, 0))
        neighbors = createNeighbors(block)
        neighbors[N.ordinal()] = null
        args << of(STONE_ID, block, neighbors, upBlocks, STONE_ID, TILE_BLOCK, DISCOVERED | VISIBLE | FILLED)

        Stream.of(args as Object[])
    }

    static MapBlock createBlock(int cid, GameBlockPos pos, long mid, int p) {
        def block = new MapBlock(cid, pos)
        block.setMaterialRid(mid)
        block.setProperties(p)
        return block
    }

    static createNeighbors(MapBlock block, NeighboringDir[] dirs = []) {
        def neighbors = new MapBlock[NeighboringDir.values().length]
        NeighboringDir.values().each {
            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), OXYGEN_ID, VISIBLE | EMPTY)
        }
        dirs.each {
            neighbors[it.ordinal()] = createBlock(1, block.pos.add(it.pos), STONE_ID, VISIBLE | FILLED)
        }
        return neighbors
    }

    static def createBlockNeighbor(MapBlock block, MapBlock[] neighbors, int cid, NeighboringDir dir, long mid, int p) {
        neighbors[dir.ordinal()] = createBlock(cid, block.pos.add(dir.pos), mid, p)
        return neighbors
    }

    @ParameterizedTest
    @MethodSource
    void ruleset_declaration(long mid, MapBlock block, MapBlock[] neighbors, MapBlock[] upBlocks, long eM, long eO, int eP) {
        log.debug "[ruleset_declaration] insert"
        session.insertAndFire(new TerrainFact(mid, block, neighbors, upBlocks))
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
