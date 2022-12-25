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
import edu.isi.powerloom.PLI
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
		"all (Sedimentary ?x)",
		"all (Metal-Ore ?type)",
		"all (metal-ore-product ?x ?y Copper)",
		"all ?x (and (melting-point-material ?x ?t) (> ?t 2000))",
		"all ?t (melting-point-material Aluminium ?t)",
		"all ?t (melting-point-material Something ?t)",
	])
	@Timeout(10)
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
	@Timeout(1000)
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
		"?t (melting-point-material Aluminium ?t)",
	])
	@Timeout(10)
	void "test ask pop"(String retrieve) {
		askKnowledgeCommandMessage({
			def answer = PLI.sAsk(retrieve, PowerLoomKnowledgeActor.WORKING_MODULE, null);
			println answer
		})
	}

	void askKnowledgeCommandMessage(def command) {
		def result =
				AskPattern.ask(
				powerLoomKnowledgeActor, {replyTo ->
					new KnowledgeCommandMessage(replyTo, command)
				},
				Duration.ofSeconds(300),
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
