/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor

import java.time.Duration

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbErrorMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.storages.MapBlockStorage
import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDB

import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * Utilities to connect, create and delete a database for testing.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class DbTestUtils {

    boolean fillDatabase = true

    def url = "remote:localhost"

    def database = "test"

    def user = "root"

    def password = "admin"

    def type = ODatabaseType.MEMORY

    OrientDB db

    Duration timeout = Duration.ofSeconds(600)

    ActorRef<Message> orientDbActor

    ActorRef<Message> objectsActor

    def scheduler

    def generator

    DbTestUtils(ActorRef<Message> orientDbActor, ActorRef<Message> objectsActor, def scheduler, def generator) {
        this.orientDbActor = orientDbActor
        this.objectsActor = objectsActor
        this.scheduler = scheduler
        this.generator = generator
    }

    void connectCreateDatabaseRemote(def initDatabaseLock) {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new ConnectDbRemoteMessage(replyTo, url, "test", "root", "admin")},
                timeout,
                scheduler)
        result.whenComplete({reply, failure ->
            log_reply_failure "connectCreateDatabaseRemote", reply, failure
            switch (reply) {
                case ConnectDbSuccessMessage:
                    db = reply.db
                    createDatabase(initDatabaseLock)
                    break
                case DbErrorMessage:
                    throw reply.error
                    break
            }
        })
    }

    void connectCreateDatabaseEmbedded(def server, def initDatabaseLock) {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new ConnectDbEmbeddedMessage(replyTo, server, "test", "root", "admin")},
                timeout,
                scheduler)
        result.whenComplete({reply, failure ->
            log_reply_failure "connectCreateDatabaseEmbedded", reply, failure
            switch (reply) {
                case ConnectDbSuccessMessage:
                    db = reply.db
                    createDatabase(initDatabaseLock)
                    break
                case DbErrorMessage:
                    throw reply.error
                    break
            }
        })
    }

    void createDatabase(def initDatabaseLock) {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new CreateDbMessage(replyTo, type)},
                timeout,
                scheduler)
        result.whenComplete( {reply, failure ->
            log_reply_failure "createDatabase", reply, failure
            createSchemas(initDatabaseLock)
        })
    }

    void createSchemas(def initDatabaseLock) {
        def result =
                AskPattern.ask(
                objectsActor,
                {replyTo -> new CreateSchemasMessage(replyTo)},
                timeout,
                scheduler)
        result.whenComplete( {reply, failure ->
            log_reply_failure "createSchemas", reply, failure
            if (fillDatabase) {
                fillDatabase(initDatabaseLock)
            } else {
                initDatabaseLock.countDown()
            }
        })
    }

    void fillDatabase(def initDatabaseLock) {
        def result =
                AskPattern.ask(
                orientDbActor, {replyTo ->
                    new DbCommandMessage(replyTo, { db ->
                        def go = new MapBlock(generator.generate())
                        go.pos = new GameBlockPos(0, 10, 20, 2)
                        go.material = 1
                        def doc = db.newVertex(go.getType());
                        db.begin();
                        new MapBlockStorage().save(doc, go)
                        doc.save();
                        db.commit();
                    })
                },
                timeout,
                scheduler)
        result.whenComplete( {reply, failure ->
            log_reply_failure "fillDatabase", reply, failure
            initDatabaseLock.countDown()
        })
    }

    void deleteDatabase(def deleteDatabaseLock) {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new DeleteDbMessage(replyTo)},
                timeout,
                scheduler)
        result.whenComplete( {reply, failure ->
            log_reply_failure "deleteDatabase", reply, failure
            deleteDatabaseLock.countDown()
        })
    }

    void closeDatabase(def closeDatabaseLock) {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new CloseDbMessage(replyTo)},
                timeout,
                scheduler)
        result.whenComplete( {reply, failure ->
            log_reply_failure "closeDatabase", reply, failure
            closeDatabaseLock.countDown()
        })
    }

    static void log_reply_failure(def name, def reply, def failure) {
        log.info "$name reply ${reply} failure ${failure}"
    }
}
