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
import static edu.isi.stella.InputStringStream.newInputStringStream;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ContextedException;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.tuple.Tuples;
import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeLoadedObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CachePutMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.IdsKnowledgeProvider.IdsKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeCommandResponseMessage.KnowledgeCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeCommandResponseMessage.KnowledgeCommandSuccessMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.RetrieveKnowledgesMessage.RetrieveKnowledgesSuccessMessage;
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
import edu.isi.powerloom.PlIterator;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.stella.Cons;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class PowerLoomKnowledgeActor implements KnowledgeGetter {

    private static boolean powerLoomLoaded = false;

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            PowerLoomKnowledgeActor.class.getSimpleName());

    public static final String NAME = PowerLoomKnowledgeActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    public static final String WORKING_MODULE = "DWARFHUSTLE-WORKING";

    /**
     * Initialize the {@link PLI} and loads all {@code *.plm} files.
     */
    public static void loadPowerLoom() throws ContextedException {
        if (powerLoomLoaded) {
            return;
        }
        var resources = new ArrayList<Pair<String, InputStream>>();
        var names = new ArrayList<String>();
        names.add("materials.plm");
        names.add("materials-sedimentary-stones.plm");
        names.add("materials-igneous-intrusive-stones.plm");
        names.add("materials-igneous-extrusive-stones.plm");
        names.add("materials-metamorphic-stones.plm");
        names.add("materials-metals.plm");
        names.add("materials-metals-ores.plm");
        names.add("materials-metals-alloys.plm");
        names.add("materials-clays.plm");
        names.add("materials-sands.plm");
        names.add("materials-seabeds.plm");
        names.add("materials-topsoils.plm");
        names.add("materials-gases.plm");
        names.add("materials-liquids.plm");
        names.add("materials-other.plm");
        names.add("game-map.plm");
        names.add("game-map-objects.plm");
        names.add("game-concepts.plm");
        names.add("game-map-objects-misc.plm");
        names.add("game-map-objects-vegetation.plm");
        names.add("game-vegetation-grasses.plm");
        names.add("game-vegetation-shrubs.plm");
        names.add("game-vegetation-trees.plm");
        names.add("game-buildings.plm");
        names.add("working.plm");
        for (var name : names) {
            resources.add(Tuples.pair(name, PowerLoomKnowledgeActor.class.getResourceAsStream(name)));
        }
        PLI.initialize();
        for (var entry : resources) {
            try {
                PLI.loadStream(newInputStringStream(IOUtils.toString(entry.getTwo(), UTF_8)), null);
            } catch (Exception e) {
                throw new ContextedException("PLI.loadStream", e).addContextValue("resource", entry.getOne());
            }
        }
        powerLoomLoaded = true;
    }

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
                @Assisted("knowledgesCache") ActorRef<Message> knowledgesCache,
                @Assisted("knowledgesGetter") ObjectsGetter knowledgesGetter);
    }

    public static Behavior<Message> create(Injector injector, CompletionStage<ActorRef<Message>> knowledgesCache,
            ObjectsGetter knowledgesGetter) {
        return Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
            var kc = knowledgesCache.toCompletableFuture().get(15, TimeUnit.SECONDS);
            return create(injector, stash, context, kc, knowledgesGetter);
        }));
    }

    public static Behavior<Message> create(Injector injector, CompletionStage<ActorRef<Message>> knowledgesCache,
            CompletionStage<ObjectsGetter> knowledgesGetter) {
        return Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
            var kc = knowledgesCache.toCompletableFuture().get(15, TimeUnit.SECONDS);
            var kg = knowledgesGetter.toCompletableFuture().get(15, TimeUnit.SECONDS);
            return create(injector, stash, context, kc, kg);
        }));
    }

    private static Behavior<Message> create(Injector injector, StashBuffer<Message> stash,
            ActorContext<Message> context, ActorRef<Message> kc, ObjectsGetter kg) {
        loadKnowledgeBase(injector, context);
        return injector.getInstance(PowerLoomKnowledgeActorFactory.class).create(context, stash, kc, kg).start();
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
            try {
                loadPowerLoom();
            } catch (Exception e) {
                log.error("loadPowerLoom", e);
                return false;
            }
            return true;
        });
    }

    /**
     * Creates the {@link PowerLoomKnowledgeActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            CompletionStage<ActorRef<Message>> knowledgesCache, ObjectsGetter objectsGetter) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, knowledgesCache, objectsGetter));
    }

    /**
     * Creates the {@link PowerLoomKnowledgeActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            CompletionStage<ActorRef<Message>> knowledgesCache, CompletionStage<ObjectsGetter> objectsGetter) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, knowledgesCache, objectsGetter));
    }

    /**
     * Retrieves the results of the query.
     */
    public static PlIterator pliRetrieve(String query) {
        return PLI.sRetrieve(query, PowerLoomKnowledgeActor.WORKING_MODULE, null);
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    @Assisted
    private StashBuffer<Message> buffer;

    @Inject
    @Assisted("knowledgesCache")
    private ActorRef<Message> knowledgesCache;

    @Inject
    @Assisted("knowledgesGetter")
    private ObjectsGetter kg;

    @Inject
    @Named("knowledge-storages")
    private Map<String, GameObjectKnowledge> storages;

    @Inject
    @Named("knowledge-tidTypeMap")
    private IntObjectMap<String> tidTypeMap;

    @Inject
    private ActorSystemProvider actor;

    @SuppressWarnings("rawtypes")
    private ActorRef<CacheResponseMessage> cacheResponseAdapter;

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
        actor.registerKnowledgeGetter(ID, this);
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
        var tid = m.type.hashCode();
        long id = KnowledgeLoadedObject.kid2Id(tid);
        knowledgesCache.tell(new CacheGetMessage<>(cacheResponseAdapter, KnowledgeLoadedObject.OBJECT_TYPE, id, go -> {
            cacheHit(m, go);
        }, () -> {
            cacheMiss(m);
        }));
        return Behaviors.same();
    }

    /**
     * Reacts to {@link KnowledgeGetMessage}. Returns a behavior for the messages
     * from {@link #getInitialBehavior()}.
     */
    @SneakyThrows
    private Behavior<Message> onRetrieveKnowledges(RetrieveKnowledgesMessage<? super Message> m) {
        log.debug("onRetrieveKnowledges {}", m);
        loadKnowledges();
        m.replyTo.tell(new RetrieveKnowledgesSuccessMessage());
        return Behaviors.same();
    }

    private void loadKnowledges() {
        tidTypeMap.keySet().forEach(tid -> {
            get(tid);
        });
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void cacheMiss(@SuppressWarnings("rawtypes") KnowledgeGetMessage m) {
        var klo = retrieveKnowledgeLoadedObject(m.type);
        cacheObjects(klo);
        m.replyTo.tell(new KnowledgeResponseSuccessMessage(klo));
        m.onSuccess.accept(klo);
    }

    @SuppressWarnings("unchecked")
    private void cacheHit(@SuppressWarnings("rawtypes") KnowledgeGetMessage m, GameObject go) {
        var klo = (KnowledgeLoadedObject) go;
        m.replyTo.tell(new KnowledgeResponseSuccessMessage(klo));
        m.onSuccess.accept(klo);
    }

    private void cacheObjects(KnowledgeLoadedObject klo) {
        knowledgesCache.tell(new CachePutMessage<>(cacheResponseAdapter, klo));
    }

    /**
     * <ul>
     * </ul>
     */
    private Behavior<Message> onWrappedCacheResponse(Object m) {
        log.debug("onWrappedCacheResponse {}", m);
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link KnowledgeCommandMessage}
     * <li>{@link KnowledgeGetMessage}
     * <li>{@link RetrieveKnowledgesMessage}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(KnowledgeCommandMessage.class, this::onKnowledgeCommand)//
                .onMessage(KnowledgeGetMessage.class, this::onKnowledgeGet)//
                .onMessage(RetrieveKnowledgesMessage.class, this::onRetrieveKnowledges)//
                .onMessage(WrappedCacheResponse.class, this::onWrappedCacheResponse)//
        ;
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
            if (!storages.containsKey(type)) {
                log.warn("No storage for object {}", type);
            } else {
                var s = storages.get(type);
                var go = s.retrieve(next, s.create());
                retrieveObjectPropertiesFromParent(s, go, type);
                list.add(go);
            }
        }
        return new KnowledgeLoadedObject(type.hashCode(), list.asUnmodifiable());
    }

    private void retrieveObjectPropertiesFromParent(GameObjectKnowledge s, KnowledgeObject go, String type) {
        var sb = new StringBuilder();
        sb.append("all (object-properties ");
        sb.append(go.getName());
        sb.append(" ?x ?y)");
        var answer = PLI.sRetrieve(sb.toString(), WORKING_MODULE, null);
        Cons next;
        while ((next = (Cons) answer.pop()) != null) {
            var parentName = ((LogicObject) (next.value)).surrogateValueInverse.symbolName;
            s.overrideProperties(parentName, go);
        }
    }

    @Override
    @SneakyThrows
    public KnowledgeLoadedObject get(int tid) {
        KnowledgeLoadedObject klo = kg.get(KnowledgeLoadedObject.OBJECT_TYPE, KnowledgeLoadedObject.kid2Id(tid));
        if (klo == null) {
            String type = tidTypeMap.get(tid);
            klo = retrieveKnowledgeLoadedObject(type);
            cacheObjects(klo);
        }
        return klo;
    }
}
