/*
 * dwarfhustle-model-actor - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.actor;

import static java.time.Duration.ofSeconds;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.map.mutable.primitive.SynchronizedIntObjectMap;

import com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.CreateAnonActorMessage;
import com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.CreateNamedActorMessage;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Dispatches messages to child actors.
 *
 * @author Erwin Müller
 */
@Slf4j
public class MainActor extends MessageActor<Message> {

    public interface MainActorFactory {
        MainActor create(ActorContext<Message> context);
    }

    public static Behavior<Message> create(Injector injector) {
        return Behaviors.setup(context -> injector.getInstance(MainActorFactory.class).create(context));
    }

    @FunctionalInterface
    public interface ActorCreate {
        CompletionStage<ActorRef<Message>> create(Duration timeout, Injector injector);
    }

    /**
     * Sends message to the actor if the actor was already created otherwise create
     * the actor and send the message.
     *
     * @param injector        the {@link Injector} that can instantiate the
     *                        {@link ActorSystemProvider}.
     * @param id              the ID of the actor.
     * @param m               the {@link Message} to send to the actor.
     * @param actorCreate     the {@link ActorCreate} callback that creates the
     *                        actor.
     * @param behaviorBuilder the {@link BehaviorBuilder} to build and return the
     *                        {@link Behavior} after the message was send.
     * @return the {@link Behavior} build from the specified {@link BehaviorBuilder}
     *         after the message was send.
     */
    public static Behavior<Message> sendMessageMayCreate(Injector injector, int id, Message m, ActorCreate actorCreate,
            BehaviorBuilder<Message> behaviorBuilder) {
        var actor = injector.getInstance(ActorSystemProvider.class).getMainActor();
        var behavior = behaviorBuilder.build();
        if (actor.haveActor(id)) {
            actor.getActor(id).tell(m);
            return behavior;
        }
        actorCreate.create(ofSeconds(1), injector).whenComplete((res, ex) -> {
            if (ex != null) {
                log.error("Error create about dialog actor", ex);
            } else {
                res.tell(m);
            }
        });
        return behavior;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> ActorRef<T> castActor(ActorRef<? extends Message> actor) {
        return (ActorRef<T>) actor;
    }

    private final MutableIntObjectMap<ActorRef<? extends Message>> namedActors;

    private final MutableList<ActorRef<? extends Message>> annonActors;

    private final ForkJoinPool pool;

    @Inject
    MainActor(@Assisted ActorContext<Message> context) {
        super(context);
        this.pool = new ForkJoinPool(4);
        this.namedActors = new SynchronizedIntObjectMap<>(IntObjectMaps.mutable.empty());
        MutableList<ActorRef<? extends Message>> annonActors = Lists.mutable.withInitialCapacity(100);
        this.annonActors = annonActors.asSynchronized();
    }

    public <T extends Message> ActorRef<T> getActor(int id) {
        return castActor(namedActors.get(id));
    }

    @SneakyThrows
    public <T extends Message> ActorRef<T> getActorAsyncNow(int id) {
        return castActor(getActorAsync(id).toCompletableFuture().get(15, TimeUnit.SECONDS));
    }

    public <T extends Message> CompletionStage<ActorRef<T>> getActorAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                waitActor(id);
            } catch (InterruptedException e) {
                log.error("getActorAsync", e);
            }
            return getActor(id);
        }, pool);
    }

    public synchronized void waitActor(int id) throws InterruptedException {
        while (!namedActors.containsKey(id)) {
            wait(10);
        }
    }

    public boolean haveActor(int id) {
        return namedActors.containsKey(id);
    }

    public void putActor(int id, ActorRef<? extends Message> actor) {
        namedActors.put(id, actor);
    }

    public void addActor(ActorRef<? extends Message> actor) {
        annonActors.add(actor);
    }

    public MainActor tell(Message m) {
        getContext().getSelf().tell(m);
        return this;
    }

    private Behavior<Message> onShutdown(ShutdownMessage m) {
        log.debug("onShutdown {}", m);
        forwardMessage(m);
        pool.shutdown();
        getContext().getSystem().terminate();
        return this;
    }

    private Behavior<Message> onCreateNamedActor(CreateNamedActorMessage m) {
        log.debug("onCreateNamedActor {}", m);
        var actor = getContext().spawnAnonymous(m.actor);
        getContext().watchWith(actor, new NamedActorTerminatedMessage(m.id, m.key, actor));
        putActor(m.id, actor);
        m.replyTo.tell(StatusReply.success(castActor(actor)));
        return this;
    }

    private Behavior<Message> onCreateAnonActor(CreateAnonActorMessage m) {
        log.debug("onCreateAnonActor {}", m);
        var actor = getContext().spawnAnonymous(m.actor);
        getContext().watchWith(actor, new AnnonActorTerminatedMessage(actor));
        addActor(actor);
        m.replyTo.tell(StatusReply.success(castActor(actor)));
        return this;
    }

    private Behavior<Message> onNamedActorTerminated(NamedActorTerminatedMessage m) {
        log.debug("onNamedActorTerminated {}", m);
        namedActors.remove(m.id);
        return this;
    }

    private Behavior<Message> onAnnonActorTerminated(AnnonActorTerminatedMessage m) {
        log.debug("onAnnonActorTerminated {}", m);
        annonActors.remove(m.actor);
        return this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Behavior<Message> forwardMessage(Message m) {
        for (ActorRef actor : namedActors) {
            actor.tell(m);
        }
        for (ActorRef actor : annonActors) {
            actor.tell(m);
        }
        return this;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()//
                .onMessage(CreateNamedActorMessage.class, this::onCreateNamedActor)//
                .onMessage(CreateAnonActorMessage.class, this::onCreateAnonActor)//
                .onMessage(NamedActorTerminatedMessage.class, this::onNamedActorTerminated)//
                .onMessage(AnnonActorTerminatedMessage.class, this::onAnnonActorTerminated)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
                .onMessage(Message.class, this::forwardMessage)//
                .build();
    }

}
