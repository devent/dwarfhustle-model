package com.anrisoftware.dwarfhustle.model.knowledge.powerloom

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.PowerLoomUtils.*

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeCommandMessage.KnowledgeCommandErrorMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeCommandMessage.KnowledgeCommandSuccessMessage
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see PowerLoomKnowledgeActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class PowerLoomKnowledgeActorTest {

	static final ActorTestKit testKit = ActorTestKit.create()

	static Injector injector

	static ActorRef<Message> powerLoomKnowledgeActor

	@BeforeAll
	static void setupActor() {
		injector = Guice.createInjector(new MainActorsModule(), new PowerloomModule())
		powerLoomKnowledgeActor = testKit.spawn(PowerLoomKnowledgeActor.create(injector), "PowerLoomKnowledgeActor");
	}

	@AfterAll
	static void closeDb() {
		testKit.shutdown(testKit.system(), Duration.ofMinutes(1))
	}

	@ParameterizedTest
	@ValueSource(strings = [
		"all (Stone ?type)",
		"all (Metal-Ore ?type)",
		"all (metal-ore-product ?x ?y Copper)",
		"all ?x (and (melting-point-material ?x ?t) (> ?t 2000))",
	])
	@Timeout(10)
	void "test retrieve"(String retrieve) {
		askKnowledgeCommandMessage({
			printPowerLoomRetrieve(retrieve, PowerLoomKnowledgeActor.WORKING_MODULE, null);
		})
	}

	void askKnowledgeCommandMessage(def command) {
		def result =
				AskPattern.ask(
				powerLoomKnowledgeActor, {replyTo ->
					new KnowledgeCommandMessage(replyTo, command)
				},
				Duration.ofSeconds(3),
				testKit.scheduler())
		def lock = new CountDownLatch(1)
		result.whenComplete( {reply, failure ->
			log.info "Command reply {} failure {}", reply, failure
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
}
