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

import static akka.actor.typed.javadsl.AskPattern.ask;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import lombok.ToString;

/**
 * Message to execute a command on the knowledge base.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class KnowledgeCommandMessage<T extends KnowledgeCommandResponseMessage> extends KnowledgeMessage<T> {

    /**
     * Asks the actor to retrieve knowledge.
     */
    public static CompletionStage<KnowledgeCommandResponseMessage> askKnowledgeGet(ActorRef<Message> a,
            Duration timeout, Scheduler scheduler, Supplier<Object> command) {
        return ask(a, replyTo -> new KnowledgeCommandMessage<>(replyTo, command), timeout, scheduler);
    }

    public final Supplier<Object> command;

    public KnowledgeCommandMessage(ActorRef<T> replyTo, Supplier<Object> command) {
        super(replyTo);
        this.command = command;
    }

}
