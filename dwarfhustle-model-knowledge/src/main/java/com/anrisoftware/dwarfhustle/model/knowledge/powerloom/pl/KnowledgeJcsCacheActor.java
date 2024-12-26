/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeLoadedObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage.CacheGetMissMessage;
import com.google.inject.Injector;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache for {@link KnowledgeLoadedObject} and {@link KnowledgeObject} game
 * knowledge objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class KnowledgeJcsCacheActor extends AbstractJcsCacheActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            KnowledgeJcsCacheActor.class.getSimpleName());

    public static final String NAME = KnowledgeJcsCacheActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    /**
     * Factory to create {@link KnowledgeJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface KnowledgeJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

        @Override
		KnowledgeJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
                TimerScheduler<Message> timer, ObjectsGetter og, ObjectsSetter os);
    }

    public static Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
            CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        return AbstractJcsCacheActor.create(injector, actorFactory, ObjectsGetter.EMPTY, ObjectsSetter.EMPTY,
                initCacheAsync);
    }

    /**
     * Creates the {@link KnowledgeJcsCacheActor}.
     *
     * @param injector the {@link Injector} injector.
     * @param timeout  the {@link Duration} timeout.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        var actorFactory = injector.getInstance(KnowledgeJcsCacheActorFactory.class);
        var initCache = createInitCacheAsync();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, initCache));
    }

    public static CompletableFuture<CacheAccess<Object, GameObject>> createInitCacheAsync() {
        CompletableFuture<CacheAccess<Object, GameObject>> initCache = CompletableFuture.supplyAsync(() -> {
            try {
                return JCS.getInstance("knowledge");
            } catch (CacheException e) {
                log.error("JCS.getInstance", e);
                throw new RuntimeException(e);
            }
        });
        return initCache;
    }

    @Override
    protected int getId() {
        return ID;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleCacheMiss(@SuppressWarnings("rawtypes") CacheGetMessage m) {
        m.replyTo.tell(new CacheGetMissMessage<>(m));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(int type, long key) {
        return (T) cache.get(key);
    }

    @Override
    protected void retrieveValueFromBackend(CacheGetMessage<?> m, Consumer<GameObject> consumer) {
        // nop
    }

    @Override
    protected void storeValueBackend(GameObject go) {
        // nop
    }

    @Override
    protected void storeValuesBackend(int objectType, Iterable<GameObject> values) {
        // nop
    }

    @Override
    protected <T extends GameObject> T getValueFromBackend(int type, long key) {
        throw new UnsupportedOperationException();
    }

}
