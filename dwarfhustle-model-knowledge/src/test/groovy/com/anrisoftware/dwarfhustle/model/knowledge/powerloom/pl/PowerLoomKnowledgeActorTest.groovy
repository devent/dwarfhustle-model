/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomTestUtils.*
import static java.time.Duration.ofSeconds
import static java.util.concurrent.CompletableFuture.supplyAsync

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeGetter
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeLoadedObject
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeCommandResponseMessage.KnowledgeCommandErrorMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeCommandResponseMessage.KnowledgeCommandSuccessMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import edu.isi.powerloom.PLI
import groovy.util.logging.Slf4j

/**
 * @see PowerLoomKnowledgeActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class PowerLoomKnowledgeActorTest {

    static Injector injector

    static cacheActor

    static ActorRef<Message> knowledgeActor

    static ActorSystemProvider actor

    @BeforeAll
    static void setupActor() {
        injector = Guice.createInjector(
                new DwarfhustleModelActorsModule(),
                new DwarfhustleModelKnowledgePowerloomPlModule(),
                new DwarfhustleModelApiObjectsModule())
        actor = injector.getInstance(ActorSystemProvider.class)
        KnowledgeJcsCacheActor.create(injector, ofSeconds(1)).whenComplete({ it, ex ->
            cacheActor = it
        } ).get()
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1),
                supplyAsync({cacheActor}),
                actor.getObjectGetterAsync(KnowledgeJcsCacheActor.ID)).
                whenComplete({ it, ex ->
                    knowledgeActor = it
                } ).get()
    }

    @AfterAll
    static void closeActor() {
        actor.shutdownWait()
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "all (work-parent-job ?X)",
        "all (work-same-job JOB-SAWMILL-PLANK ?Y)",
        "all (MATERIAL ?x)",
        "?x (work-building ?x building-carpenter)",
        "all (WORK-INPUT-UNITS JOB-SAWMILL-PLANK ?X ?Y ?Z)",
        "all (Liquid ?type)",
        "all (Gas ?type)",
        "all (Stone ?type)",
        "all (Sedimentary ?x)",
        "all (Metal-Ore ?type)",
        "all (metal-ore-product ?x ?y Copper)",
        "all (and (melting-point-material ?x ?t) (> ?t 2000))",
        "all (melting-point-material Aluminium ?t)",
        "all (melting-point-material Something ?t)",
        "all (BlockObject ?x)",
        "all (object-properties Pine-Sampling ?x ?y)",
        "all (Tree ?x)",
    ])
    @Timeout(10l)
    void "askKnowledgeCommand test retrieve"(String retrieve) {
        askKnowledgeCommandMessage({
            printPowerLoomRetrieve(retrieve, PowerLoomKnowledgeActor.WORKING_MODULE, null);
        })
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "all (Stone ?type)",
        "?t (melting-point-material Aluminium ?t)",
        "?t (thermal-conductivity-of-material Clay ?t)",
    ])
    @Timeout(10l)
    void "test retrieve pop"(String retrieve) {
        askKnowledgeCommandMessage({
            def answer = PLI.sRetrieve(retrieve, PowerLoomKnowledgeActor.WORKING_MODULE, null);
            def next
            while((next = answer.pop()) != null){
                println next
            }
        })
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "> (melting-point-material Aluminium) 2000",
        "< (melting-point-material Aluminium) 2000",
    ])
    @Timeout(10l)
    void "test ask pop"(String retrieve) {
        askKnowledgeCommandMessage({
            def answer = PLI.sAsk(retrieve, PowerLoomKnowledgeActor.WORKING_MODULE, null);
            println answer
        })
    }

    void askKnowledgeCommandMessage(def command) {
        def result =
                AskPattern.ask(
                knowledgeActor, {replyTo ->
                    new KnowledgeCommandMessage(replyTo, command)
                },
                Duration.ofSeconds(300),
                actor.scheduler)
        def lock = new CountDownLatch(1)
        result.whenComplete( {reply, failure ->
            log.info "Command reply ${reply} failure ${failure}"
            switch (reply) {
                case KnowledgeCommandSuccessMessage:
                    break
                case KnowledgeCommandErrorMessage:
                    break
            }
            lock.countDown()
        })
        lock.await()
    }

    @ParameterizedTest
    @CsvSource([
        "work-parent-job,5,4561570647800545280",
        "Sedimentary,11,7339558917942280192",
        "Shrub,1,343012349004742656",
        "BlockObject,28,-8047195809780858880"
    ])
    void "KnowledgeGetMessage test retrieve"(String type, int size, long id) {
        def result =
                AskPattern.ask(
                knowledgeActor, { replyTo ->
                    new KnowledgeGetMessage(replyTo, type)
                },
                Duration.ofSeconds(600),
                actor.scheduler)
        KnowledgeLoadedObject go
        def lock = new CountDownLatch(1)
        result.whenComplete( { reply, failure ->
            log.info "Command reply ${reply} failure ${failure}"
            if (failure == null) {
                switch (reply) {
                    case KnowledgeResponseSuccessMessage:
                        go = reply.go
                        break
                    case KnowledgeCommandErrorMessage:
                        break
                }
            }
            lock.countDown()
        })
        lock.await()
        assert go.id == id
        assert go.objects.size() == size
        println go
    }

    @ParameterizedTest
    @CsvSource([
        "Sedimentary,11,7339558917942280192",
        "Shrub,1,343012349004742656",
        "BlockObject,28,-8047195809780858880"
    ])
    void "KnowledgeGetMessage test retrieve from cache"(String type, int size, long id) {
        def result =
                AskPattern.ask(
                knowledgeActor, { replyTo ->
                    new KnowledgeGetMessage(replyTo, type)
                },
                Duration.ofSeconds(600),
                actor.scheduler)
        KnowledgeLoadedObject go
        def lock = new CountDownLatch(1)
        result.whenComplete( { reply, failure ->
            log.info "Command reply ${reply} failure ${failure}"
            if (failure == null) {
                switch (reply) {
                    case KnowledgeResponseSuccessMessage:
                        go = reply.go
                        break
                    case KnowledgeCommandErrorMessage:
                        break
                }
            }
            lock.countDown()
        })
        lock.await()
        assert go.id == id
        assert go.objects.size() == size
        result =
                AskPattern.ask(
                knowledgeActor, { replyTo ->
                    new KnowledgeGetMessage(replyTo, type)
                },
                Duration.ofSeconds(600),
                actor.scheduler)
        lock = new CountDownLatch(1)
        result.whenComplete( { reply, failure ->
            log.info "Command reply ${reply} failure ${failure}"
            if (failure == null) {
                switch (reply) {
                    case KnowledgeResponseSuccessMessage:
                        go = reply.go
                        break
                    case KnowledgeCommandErrorMessage:
                        break
                }
            }
            lock.countDown()
        })
        lock.await()
        assert go.id == id
        assert go.objects.size() == size
    }

    @ParameterizedTest
    @CsvSource([
        "Sedimentary,11,1708874227",
        "Shrub,1,79863786",
        "BlockObject,28,-1873633780",
        "Tree-Sapling,2,-919048279"
    ])
    void "KnowledgeGetMessage test getter"(String type, int size, int tid) {
        KnowledgeGetter kg = actor.getKnowledgeGetterAsync(PowerLoomKnowledgeActor.ID).get()
        KnowledgeLoadedObject go = kg.get(tid)
        assert go.id == KnowledgeLoadedObject.kid2Id(tid)
        assert go.objects.size() == size
        println go.objects
    }

    @ParameterizedTest
    @CsvSource([
        "Tree-Sapling,2,-919048279"
    ])
    void "KnowledgeGetMessage test getter with parent object-properties"(String type, int size, int tid) {
        KnowledgeGetter kg = actor.getKnowledgeGetterAsync(PowerLoomKnowledgeActor.ID).get()
        KnowledgeLoadedObject go = kg.get(tid)
        assert go.id == KnowledgeLoadedObject.kid2Id(tid)
        assert go.objects.size() == size
        println go.objects
        def pineSampling = go.objects.detect({ it.name == "PINE-SAPLING" })
        if (pineSampling) {
            assert pineSampling.name == "PINE-SAPLING"
            assert pineSampling.growingMinTemp == 10.0f
            assert pineSampling.widthMax == 5
            assert pineSampling.heightMax == 5
            assert pineSampling.depthMax == 10
            assert pineSampling.growsInto == "PINE"
        }
    }
}
