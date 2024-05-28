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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeGetMessage.askKnowledgeObjects
import static java.time.Duration.ofSeconds
import static java.util.concurrent.CompletableFuture.supplyAsync

import java.nio.file.Path

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore
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
class TerrainKnowledgeTest {

    static Injector injector

    static ActorSystemProvider actor

    static ActorRef<Message> knowledgeActor

    static ActorRef<Message> cacheActor

    static TerrainKnowledge knowledge

    static int OXYGEN_ID = 923

    static int STONE_ID = 852

    static int LIQUID_ID = 930

    static int TILE_RAMP_TRI_S = 840
    static int TILE_RAMP_TRI_W = 839
    static int TILE_RAMP_TRI_E = 838
    static int TILE_RAMP_TRI_N = 837
    static int TILE_RAMP_SINGLE    = 836
    static int TILE_RAMP_PERP_W    = 835
    static int TILE_RAMP_PERP_S    = 834
    static int TILE_RAMP_PERP_E    = 833
    static int TILE_RAMP_PERP_N    = 832
    static int TILE_RAMP_EDGE_OUT_SW = 831
    static int TILE_RAMP_EDGE_OUT_SE = 830
    static int TILE_RAMP_EDGE_OUT_NW = 829
    static int TILE_RAMP_EDGE_OUT_NE = 828
    static int TILE_RAMP_EDGE_IN_SW = 827
    static int TILE_RAMP_EDGE_IN_SE = 826
    static int TILE_RAMP_EDGE_IN_NW = 825
    static int TILE_RAMP_EDGE_IN_NE = 824
    static int TILE_RAMP_CORNER_SW = 823
    static int TILE_RAMP_CORNER_SE = 822
    static int TILE_RAMP_CORNER_NW = 821
    static int TILE_RAMP_CORNER_NE = 820
    static int TILE_BLOCK  = 819

    @TempDir
    static Path tmp

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
        this.knowledge = new TerrainKnowledge({ timeout, typeClass, type -> askKnowledgeObjects(actor.actorSystem, timeout, typeClass, type) })
    }

    static path = "/home/devent/Projects/dwarf-hustle/docu/terrain-maps"

    static MapChunksStore createStore(Path tmp, int w, int h, int d, int chunkSize, int chunksCount) {
        def name = "terrain_${w}_${h}_${d}_${chunkSize}_${chunksCount}"
        def fileName = "${path}/${name}.map"
        def stream = TerrainKnowledgeTest.class.getResourceAsStream(fileName)
        def file = tmp.resolve("${name}.map")
        IOUtils.copy(new FileInputStream(fileName), new FileOutputStream(file.toFile()))
        return new MapChunksStore(file, w, h, chunkSize, chunksCount)
    }

    @ParameterizedTest
    @MethodSource
    void terrain_create_rules(int w, int h, int d, int chunkSize, int chunksCount) {
        def store = createStore(tmp, w, h, d, chunkSize, chunksCount)
        log.debug "[terrain_create_rules] insert"
        def session = knowledge.createTerrainCreateRulesKnowledge().newStatefulSession()
        knowledge.runTerrainRules(store, session, { return TerrainFact() }, { f -> f.terrain = terrain })
        session.insertAndFire(new BlockFact(mid, block, neighbors))
        log.debug "[ruleset_declaration] done {}", block
        assert block.material == eM
        assert block.object == eO
        assert block.p.bits == eP
    }
}
