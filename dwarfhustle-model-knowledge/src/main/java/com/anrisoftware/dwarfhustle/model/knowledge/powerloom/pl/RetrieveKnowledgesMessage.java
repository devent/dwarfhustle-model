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

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import lombok.ToString;

/**
 * Message to retrieve the knowledges.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class RetrieveKnowledgesMessage<T extends Message> extends KnowledgeMessage<T> {

    /**
     * Message retrieve the knowledges is done successfully.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public static class RetrieveKnowledgesSuccessMessage extends Message {

    }

    /**
     * Asks the actor to retrieve the knowledges.
     */
    public static CompletionStage<Message> askRetrieveKnowledges(ActorSystem<Message> a, Duration timeout) {
        return askRetrieveKnowledges(a, timeout, a.scheduler());
    }

    /**
     * Asks the actor to retrieve the knowledges.
     */
    public static CompletionStage<Message> askRetrieveKnowledges(ActorRef<Message> a, Duration timeout,
            Scheduler scheduler) {
        return ask(a, replyTo -> new RetrieveKnowledgesMessage<>(replyTo), timeout, scheduler);
    }

    public RetrieveKnowledgesMessage(ActorRef<T> replyTo) {
        super(replyTo);
    }

}
