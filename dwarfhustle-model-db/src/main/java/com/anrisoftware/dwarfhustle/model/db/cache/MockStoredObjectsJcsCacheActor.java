/*
 * dwarfhustle-gamemap-jme - Game map.
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

import jakarta.inject.Inject;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.google.inject.Injector;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Returns statically available {@link GameObject}(s).
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class MockStoredObjectsJcsCacheActor extends AbstractJcsCacheActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            StoredObjectsJcsCacheActor.class.getSimpleName());

    public static final String NAME = StoredObjectsJcsCacheActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    /**
     * Factory to create {@link MockStoredObjectsJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface MockStoredObjectsJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

        @Override
        MockStoredObjectsJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
                ObjectsGetter og);
    }

    public static Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
            CompletionStage<ObjectsGetter> og, CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        return AbstractJcsCacheActor.create(injector, actorFactory, og, initCacheAsync);
    }

    /**
     * Creates the {@link MockStoredObjectsJcsCacheActor}.
     *
     * @param injector the {@link Injector} injector.
     * @param timeout  the {@link Duration} timeout.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            CompletionStage<ObjectsGetter> og) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        var actorFactory = injector.getInstance(MockStoredObjectsJcsCacheActorFactory.class);
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
    private LongObjectMap<GameObject> backend;

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
    protected void storeValueBackend(Object key, GameObject go) {
        var b = (MutableLongObjectMap<GameObject>) this.backend;
        b.put((long) key, go);
    }

    @Override
    protected void storeValueBackend(Class<?> keyType, Function<GameObject, Object> key, GameObject go) {
        var b = (MutableLongObjectMap<GameObject>) this.backend;
        b.put(go.getId(), go);
    }

    @Override
    protected void retrieveValueFromBackend(CacheGetMessage<?> m, Consumer<GameObject> consumer) {
        retrieveGameObject(m.typeClass, m.type, (long) m.key, consumer);
    }

    private void retrieveGameObject(Class<? extends GameObject> typeClass, String type, long id,
            Consumer<GameObject> consumer) {
        consumer.accept(og.get(typeClass, type, id));
    }

    @Override
    protected <T extends GameObject> T getValueFromBackend(Class<T> typeClass, String type, Object key) {
        return og.get(typeClass, type, key);
    }

    @Override
    protected BehaviorBuilder<Message> getInitialBehavior() {
        return super.getInitialBehavior()//
        ;
    }

}