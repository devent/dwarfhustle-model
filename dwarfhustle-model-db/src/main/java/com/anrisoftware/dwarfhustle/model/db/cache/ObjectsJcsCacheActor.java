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

import javax.inject.Inject;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage.DbCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage.DbCommandSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectMessage.LoadObjectErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectMessage.LoadObjectSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.SaveObjectMessage;
import com.google.inject.Injector;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache for {@link GameObject} game objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ObjectsJcsCacheActor extends AbstractJcsCacheActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            ObjectsJcsCacheActor.class.getSimpleName());

    public static final String NAME = ObjectsJcsCacheActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class WrappedObjectsResponse extends Message {
        private final ObjectsResponseMessage<?> response;
    }

    /**
     * Factory to create {@link ObjectsJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface ObjectsJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

        @Override
        ObjectsJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash, Class<?> keyType);
    }

    public static Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
            CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        return AbstractJcsCacheActor.create(injector, actorFactory, Long.class, initCacheAsync);
    }

    /**
     * Creates the {@link ObjectsJcsCacheActor}.
     *
     * @param injector the {@link Injector} injector.
     * @param timeout  the {@link Duration} timeout.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        var actorFactory = injector.getInstance(ObjectsJcsCacheActorFactory.class);
        var initCache = createInitCacheAsync();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, initCache));
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
    private ActorRef<ObjectsResponseMessage> objectsResponseAdapter;

    @Override
    protected Behavior<Message> initialStage(InitialStateMessage m) {
        log.debug("initialStage {}", m);
        this.objectsResponseAdapter = context.messageAdapter(ObjectsResponseMessage.class, WrappedObjectsResponse::new);
        return super.initialStage(m);
    }

    /**
     * <ul>
     * <li>
     * </ul>
     */
    private Behavior<Message> onWrappedObjectsResponse(WrappedObjectsResponse m) {
        log.debug("onWrappedObjectsResponse {}", m);
        var response = m.response;
        if (response instanceof LoadObjectErrorMessage<?> rm) {
            log.error("Objects error", rm);
            return Behaviors.stopped();
        } else if (response instanceof LoadObjectSuccessMessage<?> rm) {
            var lm = rm.om;
            lm.consumer.accept(rm.go);
        }
        return Behaviors.same();
    }

    @Override
    protected int getId() {
        return ID;
    }

    @Override
    protected void retrieveValueFromDb(CacheGetMessage<?> m, Consumer<GameObject> consumer) {
        if (m.key instanceof Long id) {
            retrieveGameObject(m.type, id, consumer);
        }
    }

    @Override
    protected void storeValueDb(CachePutMessage<?> m) {
        actor.tell(new SaveObjectMessage<>(objectsResponseAdapter, m.value));
    }

    @Override
    protected void storeValueDb(CachePutsMessage<?> m) {
        for (var go : m.value) {
            actor.tell(new SaveObjectMessage<>(objectsResponseAdapter, go));
        }
    }

    private void retrieveGameObject(String type, long id, Consumer<GameObject> consumer) {
        actor.tell(new LoadObjectMessage<>(objectsResponseAdapter, type, consumer, db -> {
            var query = "SELECT * from ? where objecttype = ? and objectid = ? limit 1";
            return db.query(query, type, type, id);
        }));
    }

    @Override
    @SneakyThrows
    protected GameObject retrieveValueFromDb(String type, Object key) {
        CompletionStage<DbResponseMessage<?>> result = AskPattern.ask(actor.get(),
                replyTo -> new DbCommandMessage<>(replyTo, err -> {
                    // db error
                    return null;
                }, db -> {
                    var query = "SELECT * from ? where objecttype = ? and objectid = ? limit 1";
                    return db.query(query, type, type, key);
                }), timeout, context.getSystem().scheduler());
        var res = result.toCompletableFuture().get();
        if (res instanceof DbCommandSuccessMessage<?> ret) {
            return (GameObject) ret.value;
        } else if (res instanceof DbCommandErrorMessage<?> ret) {
            log.error("retrieveValueFromDb", ret.ex);
            throw ret.ex;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected BehaviorBuilder<Message> getInitialBehavior() {
        return super.getInitialBehavior()//
                .onMessage(WrappedObjectsResponse.class, this::onWrappedObjectsResponse)//
        ;
    }
}
