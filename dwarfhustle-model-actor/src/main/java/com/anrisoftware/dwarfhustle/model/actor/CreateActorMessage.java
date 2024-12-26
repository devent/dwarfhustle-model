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

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.receptionist.ServiceKey;
import akka.pattern.StatusReply;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Abstract message to create a new actor.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public abstract class CreateActorMessage extends Message {

    @ToString.Include
    public final Behavior<? extends Message> actor;

    public final ActorRef<StatusReply<ActorRef<Message>>> replyTo;

    /**
     * Message to create a new anonymous actor.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString(callSuper = true)
    public static class CreateAnonActorMessage extends CreateActorMessage {

        public CreateAnonActorMessage(Behavior<? extends Message> actor,
                ActorRef<StatusReply<ActorRef<Message>>> replyTo) {
            super(actor, replyTo);
        }

    }

    /**
     * Message to create a new named actor.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString(onlyExplicitlyIncluded = true, callSuper = true)
    public static class CreateNamedActorMessage extends CreateActorMessage {

        public final int id;

        public final ServiceKey<Message> key;

        @ToString.Include
        public final String name;

        public CreateNamedActorMessage(int id, ServiceKey<Message> key, String name, Behavior<? extends Message> actor,
                ActorRef<StatusReply<ActorRef<Message>>> replyTo) {
            super(actor, replyTo);
            this.id = id;
            this.key = key;
            this.name = name;
        }

    }

    /**
     * Ask to create a new anonymous actor within the timeout duration.
     */
    public static CompletionStage<ActorRef<Message>> createAnonActor(ActorSystem<Message> system, Duration timeout,
            Behavior<Message> actor) {
        return AskPattern.<Message, ActorRef<Message>>askWithStatus(system,
                replyTo -> new CreateAnonActorMessage(actor, replyTo), timeout, system.scheduler());
    }

    /**
     * Ask to create a new named actor within the timeout duration. The named actor
     * is registered in the main actor.
     */
    public static CompletionStage<ActorRef<Message>> createNamedActor(ActorSystem<Message> system, Duration timeout,
            int id, ServiceKey<Message> key, String name, Behavior<? extends Message> actor) {
        return AskPattern.<Message, ActorRef<Message>>askWithStatus(system,
                replyTo -> new CreateNamedActorMessage(id, key, name, actor, replyTo), timeout, system.scheduler());
    }

}
