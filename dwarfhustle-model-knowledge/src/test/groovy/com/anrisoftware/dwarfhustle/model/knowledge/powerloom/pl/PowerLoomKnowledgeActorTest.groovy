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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomUtils.*
import static java.util.concurrent.CompletableFuture.supplyAsync
import static org.mockito.Mockito.mock

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
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
        injector = Guice.createInjector(new DwarfhustleModelActorsModule(), new DwarfhustlePowerloomModule(), new DwarfhustleModelApiObjectsModule())
        actor = injector.getInstance(ActorSystemProvider.class)
        def objectsCache = mock(ActorRef)
        PowerLoomKnowledgeActor.create(injector, Duration.ofSeconds(1), supplyAsync({objectsCache})).whenComplete({ it, ex ->
            knowledgeActor = it
        } ).get()
        KnowledgeJcsCacheActor.create(injector, Duration.ofSeconds(1), actor.getObjectsAsync(PowerLoomKnowledgeActor.ID)).whenComplete({ it, ex ->
            cacheActor = it
        } ).get()
    }

    @AfterAll
    static void closeDb() {
        actor.shutdownWait()
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "all (Special-Stone-Layer ?type)",
        "all (Gas ?type)",
        "all (Stone ?type)",
        "all (Sedimentary ?x)",
        "all (Metal-Ore ?type)",
        "all (metal-ore-product ?x ?y Copper)",
        "all (and (melting-point-material ?x ?t) (> ?t 2000))",
        "all (melting-point-material Aluminium ?t)",
        "all (melting-point-material Something ?t)",
    ])
    @Timeout(10l)
    void "test retrieve"(String retrieve) {
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

    @RepeatedTest(10)
    @Timeout(15l)
    void "test retrieve"() {
        def result =
                AskPattern.ask(
                knowledgeActor, {replyTo ->
                    new KnowledgeGetMessage(replyTo, Sedimentary.class, "Sedimentary")
                },
                Duration.ofSeconds(15),
                actor.scheduler)
        def lock = new CountDownLatch(1)
        KnowledgeLoadedObject go
        result.whenComplete( {reply, failure ->
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
        assert go.type == "Sedimentary"
        assert go.objects.size() == 11
    }
}
