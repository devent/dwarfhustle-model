/*
 * dwarfhustle-model-actor - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.actor;

import static java.time.Duration.ofSeconds;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.map.mutable.primitive.SynchronizedIntObjectMap;

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
        return Behaviors.setup((context) -> injector.getInstance(MainActorFactory.class).create(context));
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

    private final MutableIntObjectMap<ActorRef<Message>> actors;

    @Inject
    MainActor(@Assisted ActorContext<Message> context) {
        super(context);
        this.actors = new SynchronizedIntObjectMap<>(IntObjectMaps.mutable.empty());
    }

    public ActorRef<Message> getActor(int id) {
        return actors.get(id);
    }

    public boolean haveActor(int id) {
        return actors.containsKey(id);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()//
                .onMessage(CreateNamedActorMessage.class, this::onCreateNamedActor)//
                .onMessage(ActorTerminatedMessage.class, this::onActorTerminated)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
                .onMessage(Message.class, this::forwardMessage)//
                .build();
    }

    private Behavior<Message> onShutdown(ShutdownMessage m) {
        log.debug("onShutdown {}", m);
        forwardMessage(m);
        getContext().getSystem().terminate();
        return this;
    }

    private Behavior<Message> onCreateNamedActor(CreateNamedActorMessage m) {
        log.debug("onCreateNamedActor {}", m);
        var actor = getContext().spawnAnonymous(m.actor);
        getContext().watchWith(actor, new ActorTerminatedMessage(m.id, m.key, actor));
        actors.put(m.id, actor);
        m.replyTo.tell(StatusReply.success(actor));
        return this;
    }

    private Behavior<Message> onActorTerminated(ActorTerminatedMessage m) {
        log.debug("onActorTerminated {}", m);
        actors.remove(m.id);
        return this;
    }

    private Behavior<Message> forwardMessage(Message m) {
        for (ActorRef<Message> actor : actors) {
            actor.tell(m);
        }
        return this;
    }

}
