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

import java.time.Duration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.actor.ModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.map.FloorType
import com.anrisoftware.dwarfhustle.model.api.map.LightType
import com.anrisoftware.dwarfhustle.model.api.map.RoofType
import com.anrisoftware.dwarfhustle.model.api.map.TileType
import com.anrisoftware.dwarfhustle.model.api.materials.Clay
import com.anrisoftware.dwarfhustle.model.api.materials.Gas
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousExtrusive
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousIntrusive
import com.anrisoftware.dwarfhustle.model.api.materials.Metamorphic
import com.anrisoftware.dwarfhustle.model.api.materials.Sand
import com.anrisoftware.dwarfhustle.model.api.materials.Seabed
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary
import com.anrisoftware.dwarfhustle.model.api.materials.Soil
import com.anrisoftware.dwarfhustle.model.api.materials.SpecialStoneLayer
import com.anrisoftware.dwarfhustle.model.api.materials.StoneLayer
import com.anrisoftware.dwarfhustle.model.api.materials.Topsoil
import com.anrisoftware.dwarfhustle.model.api.objects.ApiModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor.KnowledgeJcsCacheActorFactory
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseErrorMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.Behaviors
import groovy.util.logging.Slf4j

/**
 * @see KnowledgeBaseActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class ListKnowledges {

    static final ActorTestKit testKit = ActorTestKit.create()

    static Injector injector

    static ActorSystemProvider actor

    static ActorRef<Message> powerLoomKnowledgeActor

    static ActorRef<Message> knowledgeBaseActor

    static ActorRef<Message> cacheActor

    @BeforeAll
    static void setupActor() {
        injector = Guice.createInjector(new ModelActorsModule(), new PowerloomModule(), new ApiModule())
        actor = injector.getInstance(ActorSystemProvider)
        powerLoomKnowledgeActor = testKit.spawn(PowerLoomKnowledgeActor.create(injector), "PowerLoomKnowledgeActor");
        cacheActor = testKit.spawn(KnowledgeJcsCacheActor.create(injector, injector.getInstance(KnowledgeJcsCacheActorFactory), KnowledgeJcsCacheActor.createInitCacheAsync()), "KnowledgeJcsCacheActor");
        knowledgeBaseActor = testKit.spawn(KnowledgeBaseActor.create(injector, powerLoomKnowledgeActor, cacheActor), "KnowledgeBaseActor");
        actor.waitMainActor()
        actor.getMainActor().putActor(KnowledgeJcsCacheActor.ID, cacheActor)
    }

    @AfterAll
    static void closeDb() {
        testKit.shutdown(testKit.system(), Duration.ofMinutes(1))
    }

    private static class WrappedKnowledgeResponse extends Message {
        KnowledgeResponseMessage response
        WrappedKnowledgeResponse(KnowledgeResponseMessage response) {
            this.response = response
        }
    }

    @Test
    @Timeout(20)
    void "list knowledge top level"() {
        def ko = []
        def knowledgeResponseAdapter
        def listKnowledge = testKit.spawn(Behaviors.setup({ context ->
            knowledgeResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class, { new WrappedKnowledgeResponse(it) });
            return Behaviors.receive(Message.class)//
                    .onMessage(WrappedKnowledgeResponse.class, {
                        switch (it.response) {
                            case KnowledgeResponseSuccessMessage:
                                println it.response.go.type
                                it.response.go.objects.each {
                                    println "${it.name},${it.rid}"
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
        }))
        while (knowledgeResponseAdapter == null) {
            Thread.sleep(10)
        }
        while (listKnowledge == null) {
            Thread.sleep(10)
        }
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, StoneLayer.class, StoneLayer.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Soil.class, Soil.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.class, Gas.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, TileType.class, TileType.TYPE))
        while (ko.size() != 56) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
    }

    @Test
    @Timeout(20)
    void "list knowledge low level"() {
        def ko = []
        def knowledgeResponseAdapter
        def listKnowledge = testKit.spawn(Behaviors.setup({ context ->
            knowledgeResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class, { new WrappedKnowledgeResponse(it) });
            return Behaviors.receive(Message.class)//
                    .onMessage(WrappedKnowledgeResponse.class, {
                        switch (it.response) {
                            case KnowledgeResponseSuccessMessage:
                                println it.response.go.type
                                it.response.go.objects.each {
                                    println "${it.name},${it.rid}"
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
        }))
        while (knowledgeResponseAdapter == null) {
            Thread.sleep(10)
        }
        while (listKnowledge == null) {
            Thread.sleep(10)
        }
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Clay.class, Clay.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.class, Gas.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousExtrusive.class, IgneousExtrusive.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousIntrusive.class, IgneousIntrusive.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Metamorphic.class, Metamorphic.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sand.class, Sand.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Seabed.class, Seabed.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sedimentary.class, Sedimentary.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, SpecialStoneLayer.class, SpecialStoneLayer.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Topsoil.class, Topsoil.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, FloorType.class, FloorType.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, LightType.class, LightType.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, RoofType.class, RoofType.TYPE))
        knowledgeBaseActor.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, TileType.class, TileType.TYPE))
        while (ko.size() != 65) {
            log.info("Knowledge objects loaded {}", ko.size())
            Thread.sleep(500)
        }
    }
}
