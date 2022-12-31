package com.anrisoftware.dwarfhustle.model.knowledge.powerloom

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage.GetReplyMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage.ReplyMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeCommandResponseMessage.KnowledgeCommandErrorMessage
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

	static ActorRef<Message> powerLoomKnowledgeActor

	static ActorRef<Message> knowledgeBaseActor

	@BeforeAll
	static void setupActor() {
		injector = Guice.createInjector(new MainActorsModule(), new PowerloomModule())
		powerLoomKnowledgeActor = testKit.spawn(PowerLoomKnowledgeActor.create(injector), "PowerLoomKnowledgeActor");
		knowledgeBaseActor = testKit.spawn(KnowledgeBaseActor.create(injector, powerLoomKnowledgeActor), "KnowledgeBaseActor");
	}

	@AfterAll
	static void closeDb() {
		testKit.shutdown(testKit.system(), Duration.ofMinutes(1))
	}

	@Test
	@Timeout(15)
	void "test retrieve"() {
		def result =
				AskPattern.ask(
				knowledgeBaseActor, {replyTo ->
					new GetReplyMessage(replyTo, "Sedimentary")
				},
				Duration.ofSeconds(15),
				testKit.scheduler())
		def lock = new CountDownLatch(1)
		def materials
		result.whenComplete( {reply, failure ->
			log.info "Command reply {} failure {}", reply, failure
			switch (reply) {
				case ReplyMessage:
					materials = reply.materials
					break
				case KnowledgeCommandErrorMessage:
					break
			}
			lock.countDown()
		})
		lock.await()
		assert materials.size() == 1
		assert materials.get("Sedimentary").size() == 11
	}
}
