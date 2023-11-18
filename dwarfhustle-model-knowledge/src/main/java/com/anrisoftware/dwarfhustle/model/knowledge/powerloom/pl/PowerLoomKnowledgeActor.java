/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;
import static edu.isi.stella.InputStringStream.newInputStringStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CachePutMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CachePutsMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.IdsKnowledgeProvider.IdsKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeCommandResponseMessage.KnowledgeCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeCommandResponseMessage.KnowledgeCommandSuccessMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.GameObjectKnowledge;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.javadsl.StashOverflowException;
import akka.actor.typed.receptionist.ServiceKey;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.logic.LogicObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class PowerLoomKnowledgeActor implements ObjectsGetter {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            PowerLoomKnowledgeActor.class.getSimpleName());

    public static final String NAME = PowerLoomKnowledgeActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    public static final String WORKING_MODULE = "DWARFHUSTLE-WORKING";

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class InitialStateMessage extends Message {
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class SetupErrorMessage extends Message {
        public final Throwable cause;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class WrappedCacheResponse extends Message {
        private final CacheResponseMessage<?> response;
    }

    /**
     * Factory to create {@link PowerLoomKnowledgeActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface PowerLoomKnowledgeActorFactory {

        PowerLoomKnowledgeActor create(ActorContext<Message> context, StashBuffer<Message> stash,
                @Assisted("knowledgeCache") ActorRef<Message> knowledgeCache,
                @Assisted("objectsCache") ActorRef<Message> objectsCache);
    }

    public static Behavior<Message> create(Injector injector, CompletionStage<ActorRef<Message>> objectsCache) {
        return Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
            loadKnowledgeBase(injector, context);
            var actor = injector.getInstance(ActorSystemProvider.class);
            var kc = actor.getActorAsync(KnowledgeJcsCacheActor.ID).toCompletableFuture().get(15, TimeUnit.SECONDS);
            var oc = objectsCache.toCompletableFuture().get(15, TimeUnit.SECONDS);
            return injector.getInstance(PowerLoomKnowledgeActorFactory.class).create(context, stash, kc, oc).start();
        }));
    }

    private static void loadKnowledgeBase(Injector injector, ActorContext<Message> context) {
        context.pipeToSelf(loadKnowledgeBase0(injector), (result, cause) -> {
            if (cause == null) {
                return new InitialStateMessage();
            } else {
                return new SetupErrorMessage(cause);
            }
        });
    }

    private static CompletionStage<Boolean> loadKnowledgeBase0(Injector injector) {
        return CompletableFuture.supplyAsync(() -> {
            var resources = new ArrayList<InputStream>();
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("game-map.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("game-map-objects.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-special-stone-layer.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-sedimentary-stones.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-igneous-intrusive-stones.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-igneous-extrusive-stones.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-metamorphic-stones.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-metals.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-metals-ores.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-metals-alloys.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-clays.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-sands.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-seabeds.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-topsoils.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-gases.plm"));
            resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("working.plm"));
            PLI.initialize();
            for (InputStream res : resources) {
                try {
                    PLI.loadStream(newInputStringStream(IOUtils.toString(res, UTF_8)), null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        });
    }

    /**
     * Creates the {@link PowerLoomKnowledgeActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            CompletionStage<ActorRef<Message>> objectsCache) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, objectsCache));
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    @Assisted
    private StashBuffer<Message> buffer;

    @Inject
    @Assisted("knowledgeCache")
    private ActorRef<Message> knowledgeCache;

    @Inject
    @Assisted("objectsCache")
    private ActorRef<Message> objectsCache;

    @Inject
    private Map<String, GameObjectKnowledge> storages;

    @SuppressWarnings("rawtypes")
    private ActorRef<CacheResponseMessage> cacheResponseAdapter;

    @Inject
    private ActorSystemProvider actor;

    @IdsKnowledge
    @Inject
    private IDGenerator ids;

    /**
     * Stash behavior. Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link InitialStateMessage}
     * <li>{@link KnowledgeMessage}
     * </ul>
     */
    public Behavior<Message> start() {
        this.cacheResponseAdapter = context.messageAdapter(CacheResponseMessage.class, WrappedCacheResponse::new);
        actor.registerObjectsGetter(ID, this);
        return Behaviors.receive(Message.class)//
                .onMessage(InitialStateMessage.class, this::onInitialState)//
                .onMessage(KnowledgeMessage.class, this::stashOtherCommand)//
                .build();
    }

    private Behavior<Message> stashOtherCommand(Message m) {
        log.debug("stashOtherCommand: {}", m);
        try {
            buffer.stash(m);
        } catch (StashOverflowException e) {
            log.warn("Stash message overflow");
        }
        return Behaviors.same();
    }

    private Behavior<Message> onInitialState(InitialStateMessage m) {
        log.debug("onInitialState");
        return buffer.unstashAll(getInitialBehavior()//
                .build());
    }

    /**
     * Reacts to {@link KnowledgeCommandMessage}. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}.
     */
    @SuppressWarnings("unchecked")
    private Behavior<Message> onKnowledgeCommand(@SuppressWarnings("rawtypes") KnowledgeCommandMessage m) {
        log.debug("onKnowledgeReplyCommand {}", m);
        try {
            var res = m.command.get();
            m.replyTo.tell(new KnowledgeCommandSuccessMessage(res));
        } catch (Exception e) {
            log.error("onKnowledgeCommand", e);
            m.replyTo.tell(new KnowledgeCommandErrorMessage(e));
        }
        return Behaviors.same();
    }

    /**
     * Reacts to {@link KnowledgeGetMessage}. Returns a behavior for the messages
     * from {@link #getInitialBehavior()}.
     */
    @SneakyThrows
    private Behavior<Message> onKnowledgeGet(KnowledgeGetMessage<?> m) {
        log.debug("onKnowledgeGet {}", m);
        knowledgeCache.tell(new CacheGetMessage<>(cacheResponseAdapter, KnowledgeLoadedObject.class,
                KnowledgeLoadedObject.OBJECT_TYPE, m.type, go -> {
                    cacheHit(m, go);
                }, () -> {
                    cacheMiss(m);
                }));
        return Behaviors.same();
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void cacheMiss(@SuppressWarnings("rawtypes") KnowledgeGetMessage m) {
        var glo = retrieveKnowledgeLoadedObject(m.type);
        cacheObjects(glo);
        knowledgeCache.tell(new CachePutMessage<>(cacheResponseAdapter, glo.type, glo));
        m.replyTo.tell(new KnowledgeResponseSuccessMessage(glo));
    }

    @SuppressWarnings("unchecked")
    private void cacheHit(@SuppressWarnings("rawtypes") KnowledgeGetMessage m, GameObject go) {
        var ko = (KnowledgeLoadedObject) go;
        cacheObjects(ko);
        m.replyTo.tell(new KnowledgeResponseSuccessMessage(ko));
    }

    private void cacheObjects(KnowledgeLoadedObject ko) {
        objectsCache.tell(new CachePutsMessage<>(cacheResponseAdapter, Long.class, GameObject::getId, ko.objects));
    }

    /**
     * <ul>
     * </ul>
     */
    private Behavior<Message> onWrappedCacheResponse(WrappedCacheResponse m) {
        log.debug("onWrappedCacheResponse {}", m);
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link KnowledgeCommandMessage}
     * <li>{@link KnowledgeGetMessage}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(KnowledgeCommandMessage.class, this::onKnowledgeCommand)//
                .onMessage(KnowledgeGetMessage.class, this::onKnowledgeGet)//
                .onMessage(WrappedCacheResponse.class, this::onWrappedCacheResponse)//
        ;
    }

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public <T extends GameObject> T get(Class<T> typeClass, String type, Object key) throws ObjectsGetterException {
        return (T) retrieveKnowledgeLoadedObject(type);
    }

    private KnowledgeLoadedObject retrieveKnowledgeLoadedObject(String type) throws GeneratorException {
        var sb = new StringBuilder();
        sb.append("all (");
        sb.append(type);
        sb.append(" ?x)");
        var answer = PLI.sRetrieve(sb.toString(), WORKING_MODULE, null);
        MutableList<KnowledgeObject> list = Lists.mutable.empty();
        LogicObject next;
        while ((next = (LogicObject) answer.pop()) != null) {
            assertThat(storages, hasKey(type));
            var s = storages.get(type);
            var go = s.retrieve(next, s.create());
            list.add(go);
        }
        return new KnowledgeLoadedObject(ids.generate(), type, list.asUnmodifiable());
    }

}
