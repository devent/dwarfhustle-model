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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl

import static java.time.Duration.ofSeconds
import static java.util.Locale.ENGLISH
import static java.util.concurrent.CompletableFuture.supplyAsync

import org.apache.commons.lang3.RegExUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
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
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseErrorMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.Behaviors
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

    @BeforeAll
    static void setupActor() {
        injector = Guice.createInjector(new DwarfhustleModelActorsModule(), new DwarfhustlePowerloomModule(), new DwarfhustleModelApiObjectsModule())
        actor = injector.getInstance(ActorSystemProvider.class)
        KnowledgeJcsCacheActor.create(injector, ofSeconds(1), actor.getObjectGetterAsync(PowerLoomKnowledgeActor.ID)).whenComplete({ it, ex ->
            cacheActor = it
        } ).get()
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1), supplyAsync({cacheActor})).whenComplete({ it, ex ->
            knowledgeActor = it
        } ).get()
    }

    @AfterAll
    static void closeDb() {
        actor.shutdownWait()
    }

    private static class WrappedKnowledgeResponse extends Message {
        KnowledgeResponseMessage response
        WrappedKnowledgeResponse(KnowledgeResponseMessage response) {
            this.response = response
        }
    }

    @Test
    @Timeout(20l)
    void "list knowledge top level"() {
        def ko = []
        def knowledgeResponseAdapter
        def listKnowledge = actor.spawn(Behaviors.setup({ context ->
            knowledgeResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class, { new WrappedKnowledgeResponse(it) });
            return Behaviors.receive(Message.class)//
                    .onMessage(WrappedKnowledgeResponse.class, {
                        switch (it.response) {
                            case KnowledgeResponseSuccessMessage:
                                println it.response.go.type
                                it.response.go.objects.each {
                                    println "${it.name},${it.kid}"
                                    ko << it
                                }
                                break
                            case KnowledgeResponseErrorMessage:
                                log.error("KnowledgeResponseErrorMessage", it.response.error)
                                break
                        }
                        Behaviors.same()
                    })//
                    .build()
        }), "listKnowledge")
        while (knowledgeResponseAdapter == null) {
            Thread.sleep(10)
        }
        while (listKnowledge == null) {
            Thread.sleep(10)
        }
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Stone.class, Stone.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Soil.class, Soil.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.class, Gas.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Liquid.class, Liquid.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, BlockType.class, BlockType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, ObjectType.class, ObjectType.TYPE))
        while (ko.size() != 85) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
    }

    @Test
    @Timeout(20l)
    void "print model-map"() {
        def ko = []
        def knowledgeResponseAdapter
        def listKnowledge = actor.spawn(Behaviors.setup({ context ->
            knowledgeResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class, { new WrappedKnowledgeResponse(it) });
            return Behaviors.receive(Message.class)//
                    .onMessage(WrappedKnowledgeResponse.class, {
                        switch (it.response) {
                            case KnowledgeResponseSuccessMessage:
                                println "rid = [:]"
                                println "\n// ${it.response.go.type}"
                                it.response.go.objects.each {
                                    println "rid[\"${it.name}\"] = ${it.kid}"
                                    ko << it
                                }
                                println "\nm = new ModelMap()\n"
                                it.response.go.objects.each { o ->
                                    def name = o.name.toLowerCase(ENGLISH)
                                    name = RegExUtils.removeAll(name, /-n$/)
                                    name = RegExUtils.removeAll(name, /-e$/)
                                    name = RegExUtils.removeAll(name, /-s$/)
                                    name = RegExUtils.removeAll(name, /-w$/)
                                    name = RegExUtils.removeAll(name, /-ne$/)
                                    name = RegExUtils.removeAll(name, /-nw$/)
                                    name = RegExUtils.removeAll(name, /-se$/)
                                    name = RegExUtils.removeAll(name, /-sw$/)
                                    name = RegExUtils.replaceAll(name, /tile-(?!block)/, "block-")
                                    println "m[rid[\"${o.name}\"]] = [model: \"Models/${name}/${name}.j3o\"]"
                                }
                                println "\nm"
                                break
                            case KnowledgeResponseErrorMessage:
                                log.error("KnowledgeResponseErrorMessage", it.response.error)
                                break
                        }
                        Behaviors.same()
                    })//
                    .build()
        }), "listKnowledge")
        while (knowledgeResponseAdapter == null) {
            Thread.sleep(10)
        }
        while (listKnowledge == null) {
            Thread.sleep(10)
        }
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, ObjectType.class, ObjectType.TYPE))
        while (ko.size() != 24) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
    }

    @Test
    @Timeout(20l)
    void "print texture-map"() {
        def ko = [:]
        def knowledgeResponseAdapter
        println "rid = [:]"
        def listKnowledge = actor.spawn(Behaviors.setup({ context ->
            knowledgeResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class, { new WrappedKnowledgeResponse(it) });
            return Behaviors.receive(Message.class)//
                    .onMessage(WrappedKnowledgeResponse.class, {
                        switch (it.response) {
                            case KnowledgeResponseSuccessMessage:
                                ko["${it.response.go.type}"] = it.response.go.objects
                                break
                            case KnowledgeResponseErrorMessage:
                                log.error("KnowledgeResponseErrorMessage", it.response.error)
                                break
                        }
                        Behaviors.same()
                    })//
                    .build()
        }), "listKnowledge")
        while (knowledgeResponseAdapter == null) {
            Thread.sleep(10)
        }
        while (listKnowledge == null) {
            Thread.sleep(10)
        }
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Clay.class, Clay.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.class, Gas.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousExtrusive.class, IgneousExtrusive.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousIntrusive.class, IgneousIntrusive.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Metamorphic.class, Metamorphic.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sand.class, Sand.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Seabed.class, Seabed.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sedimentary.class, Sedimentary.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Liquid.class, Liquid.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Topsoil.class, Topsoil.TYPE))
        while (ko.size() != 10) {
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
        def knowledgeResponseAdapter
        def listKnowledge = actor.spawn(Behaviors.setup({ context ->
            knowledgeResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class, { new WrappedKnowledgeResponse(it) });
            return Behaviors.receive(Message.class)//
                    .onMessage(WrappedKnowledgeResponse.class, {
                        switch (it.response) {
                            case KnowledgeResponseSuccessMessage:
                                println "## ${it.response.go.type}"
                                it.response.go.objects.each {
                                    println "${it.name},${it.kid}"
                                    ko << it
                                }
                                break
                            case KnowledgeResponseErrorMessage:
                                log.error("KnowledgeResponseErrorMessage", it.response.error)
                                break
                        }
                        Behaviors.same()
                    })//
                    .build()
        }), "listKnowledge")
        while (knowledgeResponseAdapter == null) {
            Thread.sleep(10)
        }
        while (listKnowledge == null) {
            Thread.sleep(10)
        }
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Clay.class, Clay.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.class, Gas.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousExtrusive.class, IgneousExtrusive.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousIntrusive.class, IgneousIntrusive.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Metamorphic.class, Metamorphic.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sand.class, Sand.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Seabed.class, Seabed.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sedimentary.class, Sedimentary.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Liquid.class, Liquid.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Topsoil.class, Topsoil.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, FloorType.class, FloorType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, LightType.class, LightType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, RoofType.class, RoofType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, BlockType.class, BlockType.TYPE))
        knowledgeActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, ObjectType.class, ObjectType.TYPE))
        while (ko.size() != 94) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
    }
}
