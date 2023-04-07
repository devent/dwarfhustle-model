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
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Timeout

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.actor.ModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.objects.ApiModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeCommandResponseMessage.KnowledgeCommandErrorMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor.KnowledgeJcsCacheActorFactory
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see KnowledgeBaseActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class KnowledgeBaseActorTest {

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

    @RepeatedTest(10)
    @Timeout(15l)
    void "test retrieve"() {
        def result =
                AskPattern.ask(
                knowledgeBaseActor, {replyTo ->
                    new KnowledgeGetMessage(replyTo, "Sedimentary")
                },
                Duration.ofSeconds(15),
                testKit.scheduler())
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
