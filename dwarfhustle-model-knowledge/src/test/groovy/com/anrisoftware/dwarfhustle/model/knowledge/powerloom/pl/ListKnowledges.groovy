/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomTestUtils.spawnListKnowledgeActor
import static com.google.inject.name.Names.named
import static java.time.Duration.ofSeconds
import static java.util.Locale.ENGLISH
import static java.util.concurrent.CompletableFuture.supplyAsync

import org.apache.commons.lang3.RegExUtils
import org.eclipse.collections.api.map.primitive.IntObjectMap
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.map.BlockObject
import com.anrisoftware.dwarfhustle.model.api.map.BlockType
import com.anrisoftware.dwarfhustle.model.api.map.FloorType
import com.anrisoftware.dwarfhustle.model.api.map.LightType
import com.anrisoftware.dwarfhustle.model.api.map.ObjectType
import com.anrisoftware.dwarfhustle.model.api.map.RoofType
import com.anrisoftware.dwarfhustle.model.api.materials.Clay
import com.anrisoftware.dwarfhustle.model.api.materials.Gas
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousExtrusive
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousIntrusive
import com.anrisoftware.dwarfhustle.model.api.materials.Liquid
import com.anrisoftware.dwarfhustle.model.api.materials.Metamorphic
import com.anrisoftware.dwarfhustle.model.api.materials.Sand
import com.anrisoftware.dwarfhustle.model.api.materials.Seabed
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary
import com.anrisoftware.dwarfhustle.model.api.materials.Soil
import com.anrisoftware.dwarfhustle.model.api.materials.Stone
import com.anrisoftware.dwarfhustle.model.api.materials.Topsoil
import com.anrisoftware.dwarfhustle.model.api.materials.Wood
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeGrass
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeShrub
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeBranch
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeLeaf
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeRoot
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeSapling
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeTrunk
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeTwig
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral

import akka.actor.typed.ActorRef
import groovy.util.logging.Slf4j

/**
 * @see knowledgeActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class ListKnowledges {

    static Injector injector

    static ActorSystemProvider actor

    static ActorRef<Message> knowledgeActor

    static ActorRef<Message> cacheActor

    static IntObjectMap<String> tidType

    @BeforeAll
    static void setupActor() {
        injector = Guice.createInjector(new DwarfhustleModelActorsModule(), new DwarfhustleModelKnowledgePowerloomPlModule(), new DwarfhustleModelApiObjectsModule())
        actor = injector.getInstance(ActorSystemProvider)
        tidType = injector.getInstance(Key.get(new TypeLiteral<IntObjectMap<String>>(){}, named("knowledge-tidTypeMap")))
        KnowledgeJcsCacheActor.create(injector, ofSeconds(1)).whenComplete({ it, ex ->
            cacheActor = it
        } ).get()
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1),
                supplyAsync({cacheActor}), actor.getObjectGetterAsync(KnowledgeJcsCacheActor.ID)).
                whenComplete({ it, ex ->
                    knowledgeActor = it
                } ).get()
    }

    @AfterAll
    static void closeDb() {
        actor.shutdownWait()
    }

    @Test
    @Timeout(20l)
    void "list knowledge top level"() {
        def ko = []
        def ret = spawnListKnowledgeActor(actor, {
            def name = tidType.get(it.response.go.id as int)
            println name
            it.response.go.objects.each {
                println "${it.name},${it.kid}"
                ko << it
            }
        })
        def knowledgeResponseAdapter = ret[1]
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Stone.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Soil.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Liquid.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, BlockType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, BlockObject.TYPE))
        while (ko.size() != 87) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
    }

    @Test
    @Timeout(20l)
    void "print model-map"() {
        def komap = [:]
        def ret = spawnListKnowledgeActor(actor, {
            def name = tidType.get(it.response.go.id as int)
            komap[name] = []
            it.response.go.objects.each { o ->
                komap[name] << o
            }
        })
        def knowledgeResponseAdapter = ret[1]
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, BlockObject.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeGrass.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeShrub.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeTreeBranch.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeTreeLeaf.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeTreeRoot.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeTreeSapling.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeTreeTrunk.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeTreeTwig.TYPE))
        while (komap.size() != 9) {
            log.info("Knowledge objects loaded {}", komap.size())
            Thread.sleep(500)
        }
        println "rid = [:]"
        komap.each { key, list ->
            println "\n// ${key}"
            list.each {
                println "rid[\"${it.name}\"] = ${it.kid}"
            }
        }
        println "\nm = new ModelMap()\n"
        komap.each { key, list ->
            list.each {
                def name = it.name.toLowerCase(ENGLISH)
                name = RegExUtils.removeAll(name, /-n$/)
                name = RegExUtils.removeAll(name, /-e$/)
                name = RegExUtils.removeAll(name, /-s$/)
                name = RegExUtils.removeAll(name, /-w$/)
                name = RegExUtils.removeAll(name, /-ne$/)
                name = RegExUtils.removeAll(name, /-nw$/)
                name = RegExUtils.removeAll(name, /-se$/)
                name = RegExUtils.removeAll(name, /-sw$/)
                name = RegExUtils.replaceAll(name, /tile-(?!block)/, "block-")
                println "m[rid[\"${it.name}\"]] = [model: \"Models/${name}/${name}.j3o\"]"
            }
        }
        println "\nm"
    }

    @Test
    @Timeout(20l)
    void "print texture-map"() {
        def ko = [:]
        println "rid = [:]"
        def ret = spawnListKnowledgeActor(actor, {
            def name = tidType.get(it.response.go.id as int)
            ko[name] = it.response.go.objects
        })
        def knowledgeResponseAdapter = ret[1]
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Clay.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousExtrusive.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousIntrusive.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Metamorphic.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sand.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Seabed.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sedimentary.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Liquid.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Topsoil.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Wood.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, KnowledgeGrass.TYPE))
        while (ko.size() != 12) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
        ko.each { type, objects ->
            println "\n// ${type}"
            objects.each {
                println "rid[\"${it.name}\"] = ${it.kid}"
            }
        }
    }

    @Test
    @Timeout(20l)
    void "list knowledge low level"() {
        def ko = []
        def ret = spawnListKnowledgeActor(actor, {
            def name = tidType.get(it.response.go.id as int)
            println "## ${name}"
            it.response.go.objects.each {
                println "${it.name},${it.kid}"
                ko << it
            }
        })
        def knowledgeResponseAdapter = ret[1]
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Clay.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousExtrusive.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousIntrusive.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Metamorphic.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sand.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Seabed.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sedimentary.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Liquid.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Topsoil.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, FloorType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, LightType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, RoofType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, BlockType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, ObjectType.TYPE))
        while (ko.size() != 115) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
    }

    @ParameterizedTest
    @CsvSource([
        "Climate-Zone,38",
        "Tree,2",
    ])
    @Timeout(20l)
    void "list knowledges"(String type, int size) {
        def ko = []
        def ret = spawnListKnowledgeActor(actor, {
            def name = tidType.get(it.response.go.id as int)
            println "## ${name}"
            it.response.go.objects.each {
                println "${it.name},${it.kid}"
                ko << it
            }
        })
        def knowledgeResponseAdapter = ret[1]
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, type))
        while (ko.size() != size) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
        ko.each {
            println it
        }
    }
}
