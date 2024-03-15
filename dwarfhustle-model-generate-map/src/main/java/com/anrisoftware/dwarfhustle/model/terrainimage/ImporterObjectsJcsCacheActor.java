/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.terrainimage;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;
import static com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor.ID;
import static com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor.KEY;
import static com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor.NAME;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.actor.SaveObjectMessage.askSaveObject;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.LoadObjectMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.SaveObjectsMessage;
import com.google.inject.Injector;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.StashBuffer;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache for {@link StoredObject} backend stored game objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ImporterObjectsJcsCacheActor extends AbstractJcsCacheActor {

    /**
     * Factory to create {@link ImporterObjectsJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface ImporterObjectsJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

        @Override
        ImporterObjectsJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
                ObjectsGetter og);
    }

    public static Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
            CompletionStage<ObjectsGetter> og, CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        return AbstractJcsCacheActor.create(injector, actorFactory, og, initCacheAsync);
    }

    /**
     * Creates the {@link ImporterObjectsJcsCacheActor}.
     *
     * @param injector the {@link Injector} injector.
     * @param timeout  the {@link Duration} timeout.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            CompletionStage<ObjectsGetter> og) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        var actorFactory = injector.getInstance(ImporterObjectsJcsCacheActorFactory.class);
        var initCache = createInitCacheAsync();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, og, initCache));
    }

    public static CompletableFuture<CacheAccess<Object, GameObject>> createInitCacheAsync() {
        CompletableFuture<CacheAccess<Object, GameObject>> initCache = supplyAsync(() -> {
            try {
                return JCS.getInstance("objects");
            } catch (CacheException e) {
                throw new RuntimeException(e);
            }
        });
        return initCache;
    }

    @Inject
    private ActorSystemProvider actor;

    @Override
    protected Behavior<Message> initialStage(InitialStateMessage m) {
        log.debug("initialStage {}", m);
        return super.initialStage(m);
    }

    @Override
    protected int getId() {
        return ID;
    }

    @Override
    protected void handleCacheMiss(@SuppressWarnings("rawtypes") CacheGetMessage m) {
        // nothing to do
    }

    @Override
    @SneakyThrows
    protected void storeValueBackend(Object key, GameObject go) {
        askSaveObject(actor.getActorSystem(), timeout, (StoredObject) go).whenComplete(this::storeValueBackendCompleted)
                .toCompletableFuture().get();
    }

    @Override
    @SneakyThrows
    protected void storeValuesBackend(String objectType, Iterable<GameObject> values) {
        SaveObjectsMessage.askSaveObjects(actor.getActorSystem(), timeout, objectType, values)
                .whenComplete(this::storeValueBackendCompleted).toCompletableFuture().get();
    }

    @Override
    protected void retrieveValueFromBackend(CacheGetMessage<?> m, Consumer<GameObject> consumer) {
        retrieveGameObject(m.type, (long) m.key, consumer);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SneakyThrows
    private void retrieveGameObject(String type, long id, Consumer consumer) {
        LoadObjectMessage.askLoadObject(actor.getActorSystem(), timeout, type, consumer, db -> {
            var query = "SELECT * from ? where objecttype = ? and objectid = ? limit 1";
            return db.query(query, type, type, id);
        }).whenComplete(this::retrieveGameObjectCompleted).toCompletableFuture().get();
    }

    @Override
    protected <T extends GameObject> T getValueFromBackend(Class<T> typeClass, String type, Object key) {
        return og.get(typeClass, type, key);
    }

    @Override
    public <T extends GameObject> T get(Class<T> typeClass, String type, Object key) {
        return super.get(typeClass, type, key);
    }

    @Override
    protected BehaviorBuilder<Message> getInitialBehavior() {
        return super.getInitialBehavior()//
        ;
    }

    private void storeValueBackendCompleted(DbResponseMessage<?> res, Throwable ex) {
        logError(res, ex);
    }

    private void retrieveGameObjectCompleted(Object res, Object ex) {
        logError((DbResponseMessage<?>) res, (Throwable) ex);
    }

    private void logError(DbResponseMessage<?> res, Throwable ex) {
        if (ex != null) {
            log.error("storeValueBackend", ex);
            context.getSelf().tell(new ShutdownMessage());
        } else {
            if (res instanceof DbErrorMessage m) {
                log.error("storeValueBackend", m.error);
                context.getSelf().tell(new ShutdownMessage());
            } else {
                // success
            }
        }
    }

}
