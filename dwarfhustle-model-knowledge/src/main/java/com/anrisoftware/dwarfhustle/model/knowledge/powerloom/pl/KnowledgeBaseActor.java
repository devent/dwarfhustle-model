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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

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
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.IdsKnowledgeProvider.IdsKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.GameObjectKnowledge;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
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
        KnowledgeBaseActor create(ActorContext<Message> context);
    }

    /**
     * Creates the knowledge actor.
     *
     * @param injector the {@link Injector}.
     */
    public static Behavior<Message> create(Injector injector) {
        return Behaviors.setup(
                context -> injector.getInstance(KnowledgeBaseActorFactory.class).create(context).start(injector));
    }

    /**
     * Creates the {@link KnowledgeBaseActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector));
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    private Map<String, GameObjectKnowledge> storages;

    @SuppressWarnings("rawtypes")
    private ActorRef<CacheResponseMessage> cacheResponseAdapter;

    @Inject
    private ActorRef<Message> actor;

    @IdsKnowledge
    @Inject
    private IDGenerator ids;

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
        return getInitialBehavior().build();
    }

    /**
     * Reacts to {@link KnowledgeGetMessage}. Returns a behavior for the messages
     * from {@link #getInitialBehavior()}.
     */
    @SneakyThrows
    private Behavior<Message> onKnowledgeGet(KnowledgeGetMessage<?> m) {
        log.debug("onKnowledgeGet {}", m);
        actor.tell(new CacheGetMessage<>(cacheResponseAdapter, KnowledgeLoadedObject.class,
                KnowledgeLoadedObject.OBJECT_TYPE, m.type, go -> {
                    cacheHit(m, go);
                }, () -> {
                    cacheMiss(m);
                }));
        return Behaviors.same();
    }

    @SuppressWarnings("unchecked")
    private void cacheMiss(@SuppressWarnings("rawtypes") KnowledgeGetMessage m) {
        var glo = retrieveKnowledgeLoadedObject(m);
        cacheObjects(glo);
        actor.tell(new CachePutMessage<>(cacheResponseAdapter, glo.type, glo));
        m.replyTo.tell(new KnowledgeResponseSuccessMessage(glo));
    }

    @SneakyThrows
    private KnowledgeLoadedObject retrieveKnowledgeLoadedObject(KnowledgeGetMessage<?> m) {
        var sb = new StringBuilder();
        sb.append("all (");
        sb.append(m.type);
        sb.append(" ?x)");
        var answer = PLI.sRetrieve(sb.toString(), WORKING_MODULE, null);
        MutableList<GameObject> list = Lists.mutable.empty();
        LogicObject next;
        while ((next = (LogicObject) answer.pop()) != null) {
            assertThat(storages, hasKey(m.type));
            var s = storages.get(m.type);
            var go = s.retrieve(next, s.create());
            go.setId((long) go.getRid());
            list.add(go);
        }
        return new KnowledgeLoadedObject(ids.generate(), m.type, list.asUnmodifiable());
    }

    @SuppressWarnings("unchecked")
    private void cacheHit(@SuppressWarnings("rawtypes") KnowledgeGetMessage m, GameObject go) {
        var ko = (KnowledgeLoadedObject) go;
        cacheObjects(ko);
        m.replyTo.tell(new KnowledgeResponseSuccessMessage(ko));
    }

    private void cacheObjects(KnowledgeLoadedObject ko) {
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
