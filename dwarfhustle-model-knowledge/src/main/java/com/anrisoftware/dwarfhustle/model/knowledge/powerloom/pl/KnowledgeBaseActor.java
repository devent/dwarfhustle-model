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
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor.WORKING_MODULE;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CachePutMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CachePutsMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeCommandResponseMessage.KnowledgeCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.GameObjectKnowledge;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
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
 * Provides specific knowledge.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class KnowledgeBaseActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            KnowledgeBaseActor.class.getSimpleName());

    public static final String NAME = KnowledgeBaseActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

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
     * Factory to create {@link KnowledgeBaseActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface KnowledgeBaseActorFactory {

        KnowledgeBaseActor create(ActorContext<Message> context, StashBuffer<Message> stash, ActorRef<Message> cache);
    }

    public static Behavior<Message> create(Injector injector, ActorRef<Message> knowledge, ActorRef<Message> cache) {
        return Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
            loadKnowledgeBase(context, knowledge);
            return injector.getInstance(KnowledgeBaseActorFactory.class).create(context, stash, cache).start(injector);
        }));
    }

    private static void loadKnowledgeBase(ActorContext<Message> context, ActorRef<Message> knowledge) {
        context.pipeToSelf(loadKnowledgeBase0(context, knowledge), (result, cause) -> {
            if (cause == null) {
                if (result instanceof KnowledgeCommandErrorMessage m) {
                    return new SetupErrorMessage(m.error);
                }
                return new InitialStateMessage();
            } else {
                return new SetupErrorMessage(cause);
            }
        });
    }

    private static CompletionStage<KnowledgeCommandResponseMessage> loadKnowledgeBase0(ActorContext<Message> context,
            ActorRef<Message> knowledge) {
        var timeout = Duration.ofSeconds(30);
        return AskPattern.ask(knowledge, replyTo -> new KnowledgeCommandMessage<>(replyTo, () -> null), timeout,
                context.getSystem().scheduler());
    }

    /**
     * Creates the {@link KnowledgeBaseActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            ActorRef<Message> knowledge, ActorRef<Message> cache) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, knowledge, cache));
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    @Assisted
    private StashBuffer<Message> buffer;

    @Inject
    private Map<String, GameObjectKnowledge> storages;

    @Inject
    private IDGenerator ids;

    @SuppressWarnings("rawtypes")
    private ActorRef<CacheResponseMessage> cacheResponseAdapter;

    @Inject
    @Assisted
    private ActorRef<Message> cache;

    @Inject
    private ActorRef<Message> actor;

    /**
     * Stash behavior. Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link InitialStateMessage}
     * <li>{@link Message}
     * </ul>
     */
    public Behavior<Message> start(Injector injector) {
        this.cacheResponseAdapter = context.messageAdapter(CacheResponseMessage.class, WrappedCacheResponse::new);
        return Behaviors.receive(Message.class)//
                .onMessage(InitialStateMessage.class, this::onInitialState)//
                .onMessage(Message.class, this::stashOtherCommand)//
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

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}.
     */
    private Behavior<Message> onInitialState(InitialStateMessage m) {
        log.debug("onInitialState");
        return buffer.unstashAll(getInitialBehavior()//
                .build());
    }

    /**
     * Reacts to {@link KnowledgeGetMessage}. Returns a behavior for the messages
     * from {@link #getInitialBehavior()}.
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private Behavior<Message> onKnowledgeGet(@SuppressWarnings("rawtypes") KnowledgeGetMessage m) {
        log.debug("onKnowledgeGet {}", m);
        cache.tell(new CacheGetMessage<>(cacheResponseAdapter, KnowledgeObject.OBJECT_TYPE, m.type, go -> {
            var ko = (KnowledgeObject) go;
            cacheObjects(ko);
            m.replyTo.tell(new KnowledgeResponseSuccessMessage(ko));
        }, () -> {
            var go = retrieveKnowledgeObject(m);
            cache.tell(new CachePutMessage<>(cacheResponseAdapter, go.type, go));
            m.replyTo.tell(new KnowledgeResponseSuccessMessage(go));
        }));
        return Behaviors.same();
    }

    private void cacheObjects(KnowledgeObject ko) {
        actor.tell(new CachePutsMessage<>(cacheResponseAdapter, Long.class, GameObject::getId, ko.objects));
    }

    /**
     * <ul>
     * </ul>
     */
    private Behavior<Message> onWrappedCacheResponse(WrappedCacheResponse m) {
        log.debug("onWrappedCacheResponse {}", m);
        return Behaviors.same();
    }

    @SneakyThrows
    private KnowledgeObject retrieveKnowledgeObject(KnowledgeGetMessage<?> m) {
        var sb = new StringBuilder();
        sb.append("all (");
        sb.append(m.type);
        sb.append(" ?x)");
        var answer = PLI.sRetrieve(sb.toString(), WORKING_MODULE, null);
        MutableList<GameObject> list = Lists.mutable.empty();
        LogicObject next;
        while ((next = (LogicObject) answer.pop()) != null) {
            var s = storages.get(m.type);
            var go = s.retrieve(next, s.create());
            go.setId(ids.generate());
            list.add(go);
        }
        return new KnowledgeObject(m.type, list.asUnmodifiable());
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link KnowledgeGetMessage}
     * <li>{@link WrappedCacheResponse}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(KnowledgeGetMessage.class, this::onKnowledgeGet)//
                .onMessage(WrappedCacheResponse.class, this::onWrappedCacheResponse)//
        ;
    }

}
