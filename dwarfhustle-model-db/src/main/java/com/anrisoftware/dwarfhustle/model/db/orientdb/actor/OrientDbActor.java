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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CloseDbMessage.CloseDbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateDbMessage.CreateDbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateDbMessage.DbAlreadyExistMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateSchemasMessage.CreateSchemasSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage.DbCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage.DbCommandSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DeleteDbMessage.DbNotExistMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.LoadObjectMessage.LoadObjectNotFoundMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.LoadObjectMessage.LoadObjectSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.LoadObjectsMessage.LoadObjectsEmptyMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.LoadObjectsMessage.LoadObjectsSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.RebuildIndexMessage.RebuildIndexSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.StartEmbeddedServerMessage.StartEmbeddedServerSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.StopEmbeddedServerMessage.StopEmbeddedServerSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameObjectSchema;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Acts on the messages:
 * <ul>
 * <li>{@link StartEmbeddedServerMessage}
 * <li>{@link StopEmbeddedServerMessage}
 * <li>{@link ConnectDbRemoteMessage}
 * <li>{@link ConnectDbEmbeddedMessage}
 * <li>{@link ShutdownMessage}
 * <li>{@link StopEmbeddedServerMessage}
 * <li>{@link CloseDbMessage}
 * <li>{@link CreateDbMessage}
 * <li>{@link DeleteDbMessage}
 * <li>{@link DbCommandMessage}
 * <li>{@link CreateSchemasMessage}</li>
 * <li>{@link LoadObjectMessage}</li>
 * <li>{@link LoadObjectsMessage}</li>
 * <li>{@link SaveObjectMessage}</li>
 * <li>{@link RebuildIndexMessage}</li>
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class OrientDbActor implements ObjectsGetter {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class, OrientDbActor.class.getSimpleName());

    public static final String NAME = OrientDbActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    /**
     * Factory to create {@link OrientDbActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface OrientDbActorFactory {

        OrientDbActor create(ActorContext<Message> context);
    }

    public static Behavior<Message> create(Injector injector) {
        return Behaviors.setup(context -> injector.getInstance(OrientDbActorFactory.class).create(context).start());
    }

    /**
     * Creates the {@link OrientDbActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector));
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    private List<GameObjectSchema> schemas;

    @Inject
    private Map<String, GameObjectStorage> storages;

    @Inject
    private ActorSystemProvider actor;

    private Optional<OrientDB> orientdb;

    private String user;

    private String password;

    private String database;

    private Optional<OServer> server;

    /**
     * Initial behavior. Returns a behavior for the messages from
     * {@link #getInitialBehavior()}.
     */
    public Behavior<Message> start() {
        this.orientdb = Optional.empty();
        this.server = Optional.empty();
        actor.registerObjectsGetter(ID, this);
        return getInitialBehavior().build();
    }

    /**
     * Reacts to {@link StartEmbeddedServerMessage}. Replies with the
     * {@link StartEmbeddedServerSuccessMessage} on success and with
     * {@link DbErrorMessage} on failure. Returns a behavior for the messages from
     * {@link #getEmbeddedServerStartedBehavior()}.
     * </ul>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onStartEmbeddedServer(StartEmbeddedServerMessage m) {
        log.debug("onStartEmbeddedServer {}", m);
        try {
            System.setProperty(Orient.ORIENTDB_HOME, m.root);
            var server = OServerMain.create();
            if (m.config != null) {
                server.startup(m.config.openStream());
            } else {
                server.startup();
            }
            server.activate();
            this.server = Optional.of(server);
        } catch (Exception e) {
            log.error("onStartEmbeddedServer", e);
            m.replyTo.tell(new DbErrorMessage(e));
            return Behaviors.same();
        }
        m.replyTo.tell(new StartEmbeddedServerSuccessMessage(server.get()));
        return getEmbeddedServerStartedBehavior().build();
    }

    /**
     * Reacts to {@link StopEmbeddedServerMessage}. Replies with the
     * {@link StopEmbeddedServerSuccessMessage} on success and with
     * {@link DbErrorMessage} on failure. Returns a behavior for the messages from
     * {@link #getInitialBehavior()}.
     * </ul>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onStopEmbeddedServer(StopEmbeddedServerMessage m) {
        log.debug("onStopEmbeddedServer {}", m);
        try {
            shutdownServer();
        } catch (Exception e) {
            log.error("onStopEmbeddedServer", e);
            m.replyTo.tell(new DbErrorMessage(e));
            return Behaviors.same();
        }
        m.replyTo.tell(new StopEmbeddedServerSuccessMessage());
        return getInitialBehavior().build();
    }

    /**
     * Reacts to {@link ConnectDbRemoteMessage}. Replies with the
     * {@link ConnectDbSuccessMessage} on success and with {@link DbErrorMessage} on
     * failure. Returns a behavior for the messages from
     * {@link #getConnectedBehavior()}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onConnectDbRemote(ConnectDbRemoteMessage m) {
        log.debug("onConnectDbRemote {}", m);
        try {
            this.user = m.user;
            this.password = m.password;
            this.database = m.database;
            var config = OrientDBConfig.defaultConfig();
            this.orientdb = Optional.of(new OrientDB(m.url, m.user, m.password, config));
        } catch (Exception e) {
            log.error("onConnectDbRemote", e);
            m.replyTo.tell(new DbErrorMessage(e));
            return Behaviors.same();
        }
        m.replyTo.tell(new ConnectDbSuccessMessage(orientdb.get()));
        return getConnectedBehavior().build();
    }

    /**
     * Reacts to {@link ConnectDbEmbeddedMessage}. Replies with the
     * {@link ConnectDbSuccessMessage} on success and with {@link DbErrorMessage} on
     * failure. Returns a behavior for the messages from
     * {@link #getConnectedBehavior()}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onConnectDbEmbedded(ConnectDbEmbeddedMessage m) {
        log.debug("onConnectDbEmbedded {}", m);
        try {
            this.user = m.user;
            this.password = m.password;
            this.database = m.database;
            this.orientdb = Optional.of(m.server.getContext());
        } catch (Exception e) {
            log.error("onConnectDbEmbedded", e);
            m.replyTo.tell(new DbErrorMessage(e));
            return Behaviors.same();
        }
        m.replyTo.tell(new ConnectDbSuccessMessage<>(orientdb.get()));
        return getConnectedBehavior().build();
    }

    /**
     * Reacts to {@link CloseDbMessage}. Returns a behavior for the messages from
     * {@link #getInitialBehavior()}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onCloseDb(CloseDbMessage m) {
        log.debug("onCloseDb {}", m);
        closeDb();
        m.replyTo.tell(new CloseDbSuccessMessage<>());
        return getInitialBehavior().build();
    }

    /**
     * Reacts to {@link CreateDbMessage}. Replies with
     * {@link CreateDbSuccessMessage} on success, with {@link DbAlreadyExistMessage}
     * if the database already exist or with {@link DbErrorMessage} on failure.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onCreateDb(CreateDbMessage m) {
        log.debug("onCreateDb {}", m);
        try {
            if (orientdb.get().exists(database)) {
                m.replyTo.tell(new DbAlreadyExistMessage<>());
            } else {
                orientdb.get().create(database, m.type);
                m.replyTo.tell(new CreateDbSuccessMessage<>());
            }
        } catch (Exception e) {
            log.error("onCreateDb", e);
            m.replyTo.tell(new DbErrorMessage<>(e));
        }
        return Behaviors.same();
    }

    /**
     * Reacts to {@link DeleteDbMessage}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onDeleteDb(DeleteDbMessage m) {
        log.debug("onDeleteDb {}", m);
        try {
            if (!orientdb.get().exists(database)) {
                m.replyTo.tell(new DbNotExistMessage<>());
            } else {
                orientdb.get().drop(database);
                m.replyTo.tell(new DbSuccessMessage<>());
            }
        } catch (Exception e) {
            log.error("onDeleteDb", e);
            m.replyTo.tell(new DbErrorMessage<>(e));
            return Behaviors.same();
        }
        return Behaviors.same();
    }

    /**
     * Reacts to {@link DbCommandMessage}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> onDbCommand(DbCommandMessage m) {
        log.debug("onDbCommand {}", m);
        try {
            Object ret = null;
            try (var db = orientdb.get().open(database, user, password)) {
                ret = m.command.apply(db);
            }
            m.replyTo.tell(new DbCommandSuccessMessage<>(ret));
        } catch (Exception e) {
            log.error("onDbCommand", e);
            m.replyTo.tell(new DbCommandErrorMessage<>(e, m.onError.apply(e)));
        }
        return Behaviors.same();
    }

    /**
     * <ul>
     * <li>
     * </ul>
     */
    private Behavior<Message> onShutdown(ShutdownMessage m) {
        log.debug("onShutdown {}", m);
        closeDb();
        shutdownServer();
        return Behaviors.stopped();
    }

    private void shutdownServer() {
        server.ifPresent(OServer::shutdown);
    }

    private void closeDb() {
        orientdb.ifPresent(o -> {
            o.close();
        });
    }

    /**
     * Returns a behavior for the messages from {@link #getConnectedBehavior()}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Behavior<Message> onCreateSchemas(CreateSchemasMessage m) {
        log.debug("onCreateSchemas {}", m);
        try (var db = orientdb.get().open(database, user, password)) {
            for (GameObjectSchema schema : schemas) {
                log.trace("createSchema {}", schema);
                schema.createSchema(db);
            }
            m.replyTo.tell(new CreateSchemasSuccessMessage<>());
        } catch (Exception e) {
            log.error("onCreateSchemas", e);
            m.replyTo.tell(new DbErrorMessage<>(e));
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages from {@link #getConnectedBehavior()}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Behavior<Message> onLoadGameObject(LoadObjectMessage<?> m) {
        log.debug("onLoadGameObject {}", m);
        LoadObjectMessage mm = m;
        try (var db = orientdb.get().open(database, user, password)) {
            var rs = m.query.apply(db);
            try {
                if (rs.hasNext()) {
                    var v = rs.next().getVertex();
                    if (v.isPresent()) {
                        var gos = storages.get(m.objectType);
                        var wm = gos.retrieve(db, v.get(), gos.create());
                        mm.replyTo.tell(new LoadObjectSuccessMessage<>(wm, m.consumer));
                    } else {
                        mm.replyTo.tell(new DbErrorMessage<>(new ObjectNotFoundException(m.objectType)));
                    }
                } else {
                    mm.replyTo.tell(new LoadObjectNotFoundMessage<>());
                }
            } finally {
                rs.close();
            }
        } catch (Exception e) {
            log.error("onLoadGameObject", e);
            mm.replyTo.tell(new DbErrorMessage<>(e));
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages from {@link #getConnectedBehavior()}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Behavior<Message> onLoadGameObjects(LoadObjectsMessage<?> m) {
        log.debug("onLoadGameObjects {}", m);
        LoadObjectsMessage mm = m;
        try (var db = orientdb.get().open(database, user, password)) {
            var rs = m.query.apply(db);
            try {
                if (!rs.hasNext()) {
                    mm.replyTo.tell(new LoadObjectsEmptyMessage<>(m.type, m.objectType));
                }
                while (rs.hasNext()) {
                    var v = rs.next().getVertex();
                    if (v.isPresent()) {
                        var gos = storages.get(m.objectType);
                        var go = gos.retrieve(db, v.get(), gos.create());
                        m.consumer.accept(go);
                    }
                }
                mm.replyTo.tell(new LoadObjectsSuccessMessage<>(m.type, m.objectType));
            } finally {
                rs.close();
            }
        } catch (Exception e) {
            log.error("onLoadGameObjects", e);
            mm.replyTo.tell(new DbErrorMessage<>(e));
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages from {@link #getConnectedBehavior()}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Behavior<Message> onSaveObject(SaveObjectMessage<?> m) {
        log.debug("onSaveObject {}", m);
        SaveObjectMessage mm = m;
        try (var db = orientdb.get().open(database, user, password)) {
            saveObject(m, db);
        } catch (Exception e) {
            log.error("onSaveObject", e);
            mm.replyTo.tell(new DbErrorMessage<>(e));
        }
        return Behaviors.same();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void saveObject(SaveObjectMessage m, ODatabaseSession db) throws Exception {
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
            m.replyTo.tell(new DbSuccessMessage<>());
        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    /**
     * Returns a behavior for the messages from {@link #getConnectedBehavior()}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Behavior<Message> onRebuildIndex(RebuildIndexMessage<?> m) {
        log.debug("onRebuildIndex {}", m);
        RebuildIndexMessage mm = m;
        try (var db = orientdb.get().open(database, user, password)) {
            db.command("REBUILD INDEX *");
            mm.replyTo.tell(new RebuildIndexSuccessMessage<>());
        } catch (Exception e) {
            log.error("onRebuildIndex", e);
            mm.replyTo.tell(new DbErrorMessage<>(e));
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link StartEmbeddedServerMessage}
     * <li>{@link StopEmbeddedServerMessage}
     * <li>{@link ConnectDbRemoteMessage}
     * <li>{@link ConnectDbEmbeddedMessage}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(StartEmbeddedServerMessage.class, this::onStartEmbeddedServer)//
                .onMessage(StopEmbeddedServerMessage.class, this::onStopEmbeddedServer)//
                .onMessage(ConnectDbRemoteMessage.class, this::onConnectDbRemote)//
                .onMessage(ConnectDbEmbeddedMessage.class, this::onConnectDbEmbedded)//
        ;
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link StopEmbeddedServerMessage}
     * <li>{@link ConnectDbRemoteMessage}
     * <li>{@link ConnectDbEmbeddedMessage}
     * </ul>
     */
    private BehaviorBuilder<Message> getEmbeddedServerStartedBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(StopEmbeddedServerMessage.class, this::onStopEmbeddedServer)//
                .onMessage(ConnectDbRemoteMessage.class, this::onConnectDbRemote)//
                .onMessage(ConnectDbEmbeddedMessage.class, this::onConnectDbEmbedded)//
        ;
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link ShutdownMessage}
     * <li>{@link StopEmbeddedServerMessage}
     * <li>{@link CloseDbMessage}
     * <li>{@link CreateDbMessage}
     * <li>{@link DeleteDbMessage}
     * <li>{@link DbCommandMessage}
     * <li>{@link CreateSchemasMessage}
     * <li>{@link LoadObjectMessage}
     * <li>{@link LoadObjectsMessage}
     * <li>{@link SaveObjectMessage}
     * <li>{@link RebuildIndexMessage}
     * </ul>
     */
    private BehaviorBuilder<Message> getConnectedBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
                .onMessage(StopEmbeddedServerMessage.class, this::onStopEmbeddedServer)//
                .onMessage(CloseDbMessage.class, this::onCloseDb)//
                .onMessage(CreateDbMessage.class, this::onCreateDb)//
                .onMessage(DeleteDbMessage.class, this::onDeleteDb)//
                .onMessage(DbCommandMessage.class, this::onDbCommand)//
                .onMessage(CreateSchemasMessage.class, this::onCreateSchemas)//
                .onMessage(LoadObjectMessage.class, this::onLoadGameObject)//
                .onMessage(LoadObjectsMessage.class, this::onLoadGameObjects)//
                .onMessage(SaveObjectMessage.class, this::onSaveObject)//
                .onMessage(RebuildIndexMessage.class, this::onRebuildIndex)//
        ;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(Class<T> typeClass, String type, Object key) {
        try (var db = orientdb.get().open(database, user, password)) {
            var query = "SELECT * from ? where objecttype = ? and objectid = ? limit 1";
            var rs = db.query(query, type, type, key);
            while (rs.hasNext()) {
                var v = rs.next().getVertex();
                if (v.isPresent()) {
                    var gos = storages.get(type);
                    var wm = gos.retrieve(db, v.get(), gos.create());
                    return (T) wm;
                }
            }
            throw new ObjectsGetterException("No item find in database");
        } catch (Exception e) {
            throw new ObjectsGetterException("Error executing query", e);
        }
    }

}
