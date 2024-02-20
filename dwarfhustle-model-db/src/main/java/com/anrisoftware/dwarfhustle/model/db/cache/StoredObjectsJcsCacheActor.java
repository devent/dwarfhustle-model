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
package com.anrisoftware.dwarfhustle.model.db.cache;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.LoadObjectMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.LoadObjectMessage.LoadObjectSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.SaveObjectMessage;
import com.google.inject.Injector;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache for {@link StoredObject} backend stored game objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class StoredObjectsJcsCacheActor extends AbstractJcsCacheActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            StoredObjectsJcsCacheActor.class.getSimpleName());

    public static final String NAME = StoredObjectsJcsCacheActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class WrappedDbResponse extends Message {
        private final DbResponseMessage<?> response;
    }

    /**
     * Factory to create {@link StoredObjectsJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface StoredObjectsJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

        @Override
        StoredObjectsJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash, ObjectsGetter og);
    }

    public static Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
            CompletionStage<ObjectsGetter> og, CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        return AbstractJcsCacheActor.create(injector, actorFactory, og, initCacheAsync);
    }

    /**
     * Creates the {@link StoredObjectsJcsCacheActor}.
     *
     * @param injector the {@link Injector} injector.
     * @param timeout  the {@link Duration} timeout.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            CompletionStage<ObjectsGetter> og) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        var actorFactory = injector.getInstance(StoredObjectsJcsCacheActorFactory.class);
        var initCache = createInitCacheAsync();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, og, initCache));
    }

    public static CompletableFuture<CacheAccess<Object, GameObject>> createInitCacheAsync() {
        CompletableFuture<CacheAccess<Object, GameObject>> initCache = CompletableFuture.supplyAsync(() -> {
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

    @SuppressWarnings("rawtypes")
    private ActorRef<DbResponseMessage> dbResponseAdapter;

    @Override
    protected Behavior<Message> initialStage(InitialStateMessage m) {
        log.debug("initialStage {}", m);
        this.dbResponseAdapter = context.messageAdapter(DbResponseMessage.class, WrappedDbResponse::new);
        return super.initialStage(m);
    }

    /**
     * <ul>
     * <li>
     * </ul>
     */
    private Behavior<Message> onWrappedDbResponse(WrappedDbResponse m) {
        log.debug("onWrappedDbResponse {}", m);
        var response = m.response;
        if (response instanceof DbErrorMessage<?> rm) {
            log.error("onWrappedDbResponse", rm.error);
            return Behaviors.stopped();
        } else if (response instanceof LoadObjectSuccessMessage<?> rm) {
            rm.consumer.accept(rm.go);
        }
        return Behaviors.same();
    }

    @Override
    protected int getId() {
        return ID;
    }

    @Override
    protected void handleCacheMiss(@SuppressWarnings("rawtypes") CacheGetMessage m) {
        if (m.key instanceof Long id && StoredObject.class.isAssignableFrom(m.typeClass)) {
            super.handleCacheMiss(m);
        }
    }

    @Override
    protected void storeValueBackend(Object key, GameObject go) {
        actor.tell(new SaveObjectMessage<>(dbResponseAdapter, (StoredObject) go));
    }

    @Override
    protected void storeValueBackend(Class<?> keyType, Function<GameObject, Object> key, GameObject go) {
        actor.tell(new SaveObjectMessage<>(dbResponseAdapter, (StoredObject) go));
    }

    @Override
    protected void retrieveValueFromBackend(CacheGetMessage<?> m, Consumer<GameObject> consumer) {
        retrieveGameObject(m.type, (long) m.key, consumer);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void retrieveGameObject(String type, long id, Consumer consumer) {
        actor.tell(new LoadObjectMessage(dbResponseAdapter, type, consumer, db -> {
            var query = "SELECT * from ? where objecttype = ? and objectid = ? limit 1";
            return ((ODatabaseDocument) db).query(query, type, type, id);
        }));
    }

    @Override
    protected <T extends GameObject> T getValueFromBackend(Class<T> typeClass, String type, Object key) {
        return og.get(typeClass, type, key);
    }

    @Override
    protected BehaviorBuilder<Message> getInitialBehavior() {
        return super.getInitialBehavior()//
                .onMessage(WrappedDbResponse.class, this::onWrappedDbResponse)//
        ;
    }

}
