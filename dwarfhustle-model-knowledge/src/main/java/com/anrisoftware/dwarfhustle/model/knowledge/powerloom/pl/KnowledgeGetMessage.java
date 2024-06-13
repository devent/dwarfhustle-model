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

import static akka.actor.typed.javadsl.AskPattern.ask;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.eclipse.collections.api.list.ListIterable;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * Message to retrieve knowledge. Replies with a
 * {@link KnowledgeResponseSuccessMessage} on success or with
 * {@link KnowledgeResponseErrorMessage} on error.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class KnowledgeGetMessage<T extends Message> extends KnowledgeMessage<T> {

    /**
     * Asks the actor to retrieve knowledge.
     */
    public static CompletionStage<KnowledgeResponseMessage> askKnowledgeGet(ActorRef<Message> a, Duration timeout,
            Scheduler scheduler, Class<? extends GameObject> typeClass, String type) {
        return ask(a, replyTo -> new KnowledgeGetMessage<>(replyTo, typeClass, type), timeout, scheduler);
    }

    /**
     * Asks the actor to retrieve knowledge.
     */
    public static CompletionStage<KnowledgeResponseMessage> askKnowledgeGet(ActorSystem<Message> a, Duration timeout,
            Class<? extends GameObject> typeClass, String type) {
        return askKnowledgeGet(a, timeout, a.scheduler(), typeClass, type);
    }

    /**
     * Asks the actor to retrieve {@link KnowledgeObject}s.
     */
    @SneakyThrows
    public static CompletionStage<ListIterable<KnowledgeObject>> askKnowledgeObjects(ActorRef<Message> a,
            Duration timeout, Scheduler scheduler, Class<? extends GameObject> typeClass, String type) {
        return askKnowledgeGet(a, timeout, scheduler, typeClass, type).handle((res, ex) -> {
            if (ex != null) {
                throw new RuntimeException(ex);
            } else {
                if (res instanceof KnowledgeResponseSuccessMessage rm) {
                    return rm.go.objects;
                } else if (res instanceof KnowledgeResponseErrorMessage rm) {
                    throw new RuntimeException(rm.error);
                }
            }
            return null;
        });
    }

    /**
     * Asks the actor to retrieve {@link KnowledgeObject}s.
     */
    @SneakyThrows
    public static CompletionStage<ListIterable<KnowledgeObject>> askKnowledgeObjects(ActorSystem<Message> a,
            Duration timeout, Class<? extends GameObject> typeClass, String type) {
        return askKnowledgeObjects(a, timeout, a.scheduler(), typeClass, type);
    }

    public final Class<? extends GameObject> typeClass;

    public final String type;

    public KnowledgeGetMessage(ActorRef<T> replyTo, Class<? extends GameObject> typeClass, String type) {
        super(replyTo);
        this.typeClass = typeClass;
        this.type = type;
    }
}
