package com.anrisoftware.dwarfhustle.model.knowledge.db.orientdb

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.knowledge.db.orientdb.ConnectDbMessage.ConnectDbErrorMessage
import com.anrisoftware.dwarfhustle.model.knowledge.db.orientdb.ConnectDbMessage.ConnectDbSuccessMessage
import com.google.inject.Guice
import com.google.inject.Injector
import com.orientechnologies.orient.core.db.ODatabaseType

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see OrientDbActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class OrientDbActorTest {

    static final ActorTestKit testKit = ActorTestKit.create()

    static Injector injector

    static ActorRef<Message> orientDbActor

    @BeforeAll
    static void setupActor() {
        injector = Guice.createInjector(new MainActorsModule(), new OrientDbModule())
        orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
    }

    @AfterAll
    static void closeDb() {
        orientDbActor.tell(new CloseDbMessage())
        testKit.shutdown(testKit.system(), Duration.ofMinutes(1))
    }

    @Test
    void connect_db_message_create_db() {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new ConnectDbMessage(replyTo, "remote:172.21.0.2", "root", "admin")},
                Duration.ofSeconds(3),
                testKit.scheduler())
        def lock = new CountDownLatch(1)
        result.whenComplete( {reply, failure ->
            log.info "Connect database reply {} failure {}", reply, failure
            switch (reply) {
                case ConnectDbSuccessMessage:
                    createDatabase(lock)
                    break
                case ConnectDbErrorMessage:
                    break
            }
        })
        lock.await()
    }

    void createDatabase(def lock) {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new CreateDbMessage(replyTo, "test", ODatabaseType.PLOCAL)},
                Duration.ofSeconds(10),
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log.info "Create database reply {} failure {}", reply, failure
            executeCommand(lock)
        })
    }

    void executeCommand(def lock) {
        def result =
                AskPattern.ask(
                orientDbActor, {replyTo ->
                    new DbCommandMessage(replyTo, "test", "root", "admin", { db ->
                        def cl = db.createClassIfNotExist("Person", "V")
                        db.begin();
                        def doc = db.newVertex(cl);
                        doc.setProperty("name", "John");
                        doc.save();
                        db.commit()
                    })
                },
                Duration.ofSeconds(10),
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log.info "Execute command reply {} failure {}", reply, failure
            lock.countDown()
        })
    }
}
