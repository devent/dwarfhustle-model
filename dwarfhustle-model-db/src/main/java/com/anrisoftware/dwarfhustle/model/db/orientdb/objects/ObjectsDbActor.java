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
package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage.DbCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage.DbCommandSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.CreateSchemasMessage.CreatedSchemasErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.CreateSchemasMessage.CreatedSchemasSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectMessage.LoadObjectErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectMessage.LoadObjectSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectsMessage.LoadObjectsErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectsMessage.LoadObjectsSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.SaveObjectMessage.SaveObjectErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameObjectSchema;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Acts on the messages:
 * <ul>
 * <li>{@link CreateSchemasMessage}</li>
 * <li>{@link LoadObjectMessage}</li>
 * <li>{@link LoadObjectsMessage}</li>
 * <li>{@link SaveObjectMessage}</li>
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ObjectsDbActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            ObjectsDbActor.class.getSimpleName());

    public static final String NAME = ObjectsDbActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class WrappedDbResponse extends Message {
        private final DbResponseMessage<?> response;
    }

    /**
     * Factory to create {@link ObjectsDbActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface ObjectsDbActorFactory {
        ObjectsDbActor create(ActorContext<Message> context, ActorRef<Message> db);
    }

    /**
     * Creates the {@link ObjectsDbActor}.
     */
    public static Behavior<Message> create(Injector injector, ActorRef<Message> db) {
        return Behaviors
                .setup(context -> injector.getInstance(ObjectsDbActorFactory.class).create(context, db).start());
    }

    /**
     * Creates the {@link ObjectsDbActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout, ActorRef<Message> db) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, db));
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    @Assisted
    private ActorRef<Message> db;

    @Inject
    private List<GameObjectSchema> schemas;

    @Inject
    private Map<String, GameObjectStorage> storages;

    @SuppressWarnings("rawtypes")
    private ActorRef<DbResponseMessage> dbResponseAdapter;

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}
     */
    public Behavior<Message> start() {
        this.dbResponseAdapter = context.messageAdapter(DbResponseMessage.class, WrappedDbResponse::new);
        return getInitialBehavior()//
                .build();
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}
     */
    private Behavior<Message> onCreateSchemas(CreateSchemasMessage<?> m) {
        log.debug("onCreateSchemas {}", m);
        db.tell(new DbCommandMessage<>(dbResponseAdapter, ex -> new CreatedSchemasErrorMessage<>(m, ex), db -> {
            createSchemas(db);
            return new CreatedSchemasSuccessMessage<>(m);
        }));
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}
     */
    private Behavior<Message> onLoadGameObject(LoadObjectMessage<?> m) {
        log.debug("onLoadGameObject {}", m);
        db.tell(new DbCommandMessage<>(dbResponseAdapter, ex -> new LoadObjectErrorMessage<>(m, ex), db -> {
            var wm = loadGameObject(m, db);
            return new LoadObjectSuccessMessage<>(m, wm);
        }));
        return Behaviors.same();
    }

    @SneakyThrows
    private GameObject loadGameObject(LoadObjectMessage<?> m, ODatabaseDocument db) {
        var rs = m.query.apply(db);
        try {
            while (rs.hasNext()) {
                var v = rs.next().getVertex();
                if (v.isPresent()) {
                    var gos = storages.get(m.objectType);
                    var wm = gos.retrieve(db, v.get(), gos.create());
                    return wm;
                }
            }
        } finally {
            rs.close();
        }
        throw new LoadObjectException("no game object found");
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}
     */
    private Behavior<Message> onLoadGameObjects(LoadObjectsMessage<?> m) {
        log.debug("onLoadGameObjects {}", m);
        db.tell(new DbCommandMessage<>(dbResponseAdapter, ex -> new LoadObjectsErrorMessage<>(m, ex), db -> {
            loadGameObjects(m, db);
            return null;
        }));
        return Behaviors.same();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SneakyThrows
    private void loadGameObjects(LoadObjectsMessage m, ODatabaseDocument db) {
        var rs = (OResultSet) m.query.apply(db);
        try {
            while (rs.hasNext()) {
                var v = rs.next().getVertex();
                if (v.isPresent()) {
                    var gos = storages.get(m.objectType);
                    var go = gos.retrieve(db, v.get(), gos.create());
                    m.consumer.accept(go);
                }
            }
            m.replyTo.tell(new LoadObjectsSuccessMessage(m));
        } finally {
            rs.close();
        }
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}
     */
    private Behavior<Message> onSaveObject(SaveObjectMessage<?> m) {
        log.debug("onSaveObject {}", m);
        db.tell(new DbCommandMessage<>(dbResponseAdapter, ex -> new SaveObjectErrorMessage<>(m, ex), db -> {
            saveGameObjects(m, db);
            return null;
        }));
        return Behaviors.same();
    }

    private void saveGameObjects(SaveObjectMessage<?> m, ODatabaseDocument db) {
        if (!m.go.isDirty()) {
            return;
        }
        OElement v = null;
        var arid = m.go.getRid();
        if (arid instanceof ORID rid) {
            v = db.load(rid);
        } else {
            v = db.newVertex(m.go.getObjectType());
        }
        db.begin();
        try {
            storages.get(m.go.getObjectType()).store(db, v, m.go);
            v.save();
            db.commit();
            if (arid == null) {
                m.go.setRid(v.getIdentity());
            }
        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    /**
     * <ul>
     * <li>Stops the actor on {@link DbErrorMessage} and replies with
     * {@link ObjectsErrorMessage}.</li>
     * <li>Returns a behavior for the messages from {@link #getInitialBehavior()} on
     * {@link DbSuccessMessage} and replies with {@link ObjectsSuccessMessage}.</li>
     * </ul>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onWrappedDbResponse(WrappedDbResponse m) {
        log.debug("onWrappedDbResponse {}", m);
        var response = m.response;
        if (response instanceof DbCommandErrorMessage rm) {
            log.error("Db error", rm);
            if (rm.onError instanceof CreatedSchemasErrorMessage res) {
                res.om.replyTo.tell(res);
            } else if (rm.onError instanceof ObjectsErrorMessage res) {
                res.om.replyTo.tell(res);
            }
            return Behaviors.stopped();
        } else if (response instanceof DbCommandSuccessMessage rm) {
            if (rm.value instanceof CreatedSchemasSuccessMessage res) {
                res.om.replyTo.tell(res);
            } else if (rm.value instanceof LoadObjectSuccessMessage res) {
                res.om.replyTo.tell(res);
            }
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link CreateSchemasMessage}
     * <li>{@link LoadObjectMessage}
     * <li>{@link LoadObjectsMessage}
     * <li>{@link SaveObjectMessage}
     * <li>{@link WrappedDbResponse}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(CreateSchemasMessage.class, this::onCreateSchemas)//
                .onMessage(LoadObjectMessage.class, this::onLoadGameObject)//
                .onMessage(LoadObjectsMessage.class, this::onLoadGameObjects)//
                .onMessage(SaveObjectMessage.class, this::onSaveObject)//
                .onMessage(WrappedDbResponse.class, this::onWrappedDbResponse)//
        ;
    }

    private void createSchemas(ODatabaseDocument db) {
        for (GameObjectSchema schema : schemas) {
            log.trace("createSchema {}", schema);
            schema.createSchema(db);
        }
    }

}
