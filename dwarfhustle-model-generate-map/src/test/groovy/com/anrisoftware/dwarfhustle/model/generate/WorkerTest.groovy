package com.anrisoftware.dwarfhustle.model.generate

import static com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils.*

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.lable.oss.uniqueid.IDGenerator

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.ApiModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CloseDbMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsModule
import com.anrisoftware.dwarfhustle.model.generate.Worker.WorkerFactory
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.PowerLoomKnowledgeActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.PowerloomModule
import com.anrisoftware.globalpom.threads.properties.internal.PropertiesThreadsModule
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import groovy.util.logging.Slf4j

/**
 * @see KnowledgeBaseActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class WorkerTest {

	static final ActorTestKit testKit = ActorTestKit.create()

	static Injector injector

	static ActorRef<Message> orientDbActor

	static ActorRef<Message> objectsActor

	static ActorRef<Message> powerLoomKnowledgeActor

	static ActorRef<Message> knowledgeBaseActor

	static WorkerFactory workerFactory

	static timeout = Duration.ofSeconds(300)

	static initDatabaseLock = new CountDownLatch(1)

	static deleteDatabaseLock = new CountDownLatch(1)

	@BeforeAll
	static void setupActor() {
		fillDatabase = false
		injector = Guice.createInjector(
				new MainActorsModule(), new ObjectsModule(), new PowerloomModule(), new GenerateModule(), new OrientDbModule(), new ApiModule(),
				new PropertiesThreadsModule())
		workerFactory = injector.getInstance(WorkerFactory)
		orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
		objectsActor = testKit.spawn(ObjectsActor.create(injector, orientDbActor), "objectsActor");
		powerLoomKnowledgeActor = testKit.spawn(PowerLoomKnowledgeActor.create(injector), "PowerLoomKnowledgeActor");
		knowledgeBaseActor = testKit.spawn(KnowledgeBaseActor.create(injector, powerLoomKnowledgeActor), "KnowledgeBaseActor");
		connectCreateDatabase(orientDbActor, objectsActor, testKit, injector.getInstance(IDGenerator.class), initDatabaseLock)
		initDatabaseLock.await()
	}

	@AfterAll
	static void closeDb() {
		deleteDatabase(orientDbActor, testKit, deleteDatabaseLock)
		orientDbActor.tell(new CloseDbMessage())
		testKit.shutdown(testKit.system(), timeout)
		deleteDatabaseLock.await()
	}

	@Test
	@Timeout(600)
	void "test generate"() {
		def s = 32
		def m = new GenerateMapMessage(null, database, user, password, 0, s, s, s)
		workerFactory.create(db).generateMap(m)
	}
}
