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
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.collections.api.list.ListIterable;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeLoadedObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * Message to retrieve {@link KnowledgeLoadedObject}(s). Replies with a
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
            Scheduler scheduler, String type) {
        return ask(a, replyTo -> new KnowledgeGetMessage<>(replyTo, type), timeout, scheduler);
    }

    /**
     * Asks the actor to retrieve knowledge.
     *
     * @param a       the {@link ActorSystem}.
     * @param timeout the timeout {@link Duration}.
     * @param the     type {@link String} from
     *                {@link KnowledgeObject#getKnowledgeType()}.
     */
    public static CompletionStage<KnowledgeResponseMessage> askKnowledgeGet(ActorSystem<Message> a, Duration timeout,
            String type) {
        return askKnowledgeGet(a, timeout, a.scheduler(), type);
    }

    /**
     * Asks the actor to retrieve {@link KnowledgeObject}s.
     *
     * @param a         the {@link ActorRef}.
     * @param timeout   the timeout {@link Duration}.
     * @param scheduler the {@link Scheduler}.
     * @param the       type {@link String} from
     *                  {@link KnowledgeObject#getKnowledgeType()}.
     */
    @SneakyThrows
    public static CompletionStage<ListIterable<KnowledgeObject>> askKnowledgeObjects(ActorRef<Message> a,
            Duration timeout, Scheduler scheduler, String type) {
        return askKnowledgeGet(a, timeout, scheduler, type).handle((res, ex) -> {
            if (ex == null) {
                if (res instanceof KnowledgeResponseSuccessMessage rm) {
                    return rm.go.objects;
                } else if (res instanceof KnowledgeResponseErrorMessage rm) {
                    throw new RuntimeException(rm.error);
                }
            } else {
                throw new RuntimeException(ex);
            }
            return null;
        });
    }

    /**
     * Ask the actor to retrieve the specific {@link KnowledgeObject}.
     *
     * @see KnowledgeObject
     */
    public static <T extends KnowledgeObject> T askKnowledgeObject(ActorSystem<Message> a, Duration timeout,
            Class<T> clazz, String type, Predicate<T> predicate)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeObject(a, timeout, a.scheduler(), clazz, type, predicate);
    }

    /**
     * Ask the actor to retrieve the specific {@link KnowledgeObject}.
     *
     * @see KnowledgeObject
     */
    @SuppressWarnings("unchecked")
    public static <T extends KnowledgeObject> T askKnowledgeObject(ActorRef<Message> a, Duration timeout,
            Scheduler scheduler, Class<T> clazz, String type, Predicate<T> predicate)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeObjects(a, timeout, scheduler, type).toCompletableFuture()
                .get(timeout.toMillis(), MILLISECONDS).collectIf(o -> {
                    final var ko = (T) o;
                    return predicate.test(ko);
                }, ko -> (T) ko.getAsType()).getFirst();
    }

    /**
     * Ask the actor to retrieve the specific {@link KnowledgeObject}.
     *
     * @see KnowledgeObject
     */
    public static <T extends KnowledgeObject> ListIterable<T> askKnowledgeObjects(ActorSystem<Message> a,
            Duration timeout, Class<T> clazz, String type, Predicate<T> predicate)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeObjects(a, timeout, a.scheduler(), clazz, type, predicate);
    }

    /**
     * Ask the actor to retrieve the specific {@link KnowledgeObject}.
     *
     * @see KnowledgeObject
     */
    @SuppressWarnings("unchecked")
    public static <T extends KnowledgeObject> ListIterable<T> askKnowledgeObjects(ActorRef<Message> a, Duration timeout,
            Scheduler scheduler, Class<T> clazz, String type, Predicate<T> predicate)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeObjects(a, timeout, scheduler, type).toCompletableFuture()
                .get(timeout.toMillis(), MILLISECONDS).collectIf(o -> {
                    final var ko = (T) o;
                    return predicate.test(ko);
                }, ko -> (T) ko.getAsType());
    }

    /**
     * Ask the actor to retrieve the specific {@link KnowledgeObject}.
     *
     * @see KnowledgeObject
     */
    public static <T extends KnowledgeObject> T askKnowledgeObjectName(ActorSystem<Message> a, Duration timeout,
            Class<T> clazz, String type, String name)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeObjectName(a, timeout, a.scheduler(), clazz, type, name);
    }

    /**
     * Ask the actor to retrieve the specific {@link KnowledgeObject}.
     *
     * @see KnowledgeObject
     */
    public static <T extends KnowledgeObject> T askKnowledgeObjectName(ActorRef<Message> a, Duration timeout,
            Scheduler scheduler, Class<T> clazz, String type, String name)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeObject(a, timeout, scheduler, clazz, type, ko -> ko.getName().equalsIgnoreCase(name));
    }

    /**
     * Ask the actor to retrieve the ID of a specific {@link KnowledgeObject}.
     *
     * @see KnowledgeObject
     */
    public static <T extends KnowledgeObject> long askKnowledgeId(ActorSystem<Message> a, Duration timeout,
            Class<T> clazz, String type, Predicate<T> predicate)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeId(a, timeout, a.scheduler(), clazz, type, predicate);
    }

    /**
     * Ask the actor to retrieve the ID of a specific {@link KnowledgeObject}.
     *
     * @see KnowledgeObject
     */
    public static <T extends KnowledgeObject> long askKnowledgeId(ActorRef<Message> a, Duration timeout,
            Scheduler scheduler, Class<T> clazz, String type, Predicate<T> predicate)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeObjects(a, timeout, scheduler, type).toCompletableFuture()
                .get(timeout.toMillis(), MILLISECONDS).collectIf(o -> {
                    @SuppressWarnings("unchecked")
                    final var ko = (T) o;
                    return predicate.test(ko);
                }, KnowledgeObject::getId).getFirst();
    }

    /**
     * Ask the actor to retrieve the ID of a {@link KnowledgeObject} by name.
     *
     * @see KnowledgeObject
     */
    public static long askKnowledgeIdByName(ActorSystem<Message> a, Duration timeout, String type, String name)
            throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeIdByName(a, timeout, a.scheduler(), type, name);
    }

    /**
     * Ask the actor to retrieve the ID of a {@link KnowledgeObject} by name.
     *
     * @see KnowledgeObject
     */
    public static long askKnowledgeIdByName(ActorRef<Message> a, Duration timeout, Scheduler scheduler, String type,
            String name) throws InterruptedException, ExecutionException, TimeoutException {
        return askKnowledgeId(a, timeout, scheduler, KnowledgeObject.class, type,
                ko -> ko.getName().equalsIgnoreCase(name));
    }

    /**
     * Asks the actor to retrieve {@link KnowledgeObject}s.
     */
    @SneakyThrows
    public static CompletionStage<ListIterable<KnowledgeObject>> askKnowledgeObjects(ActorSystem<Message> a,
            Duration timeout, String type) {
        return askKnowledgeObjects(a, timeout, a.scheduler(), type);
    }

    private final static Consumer<KnowledgeLoadedObject> NOP = ko -> {
    };

    public final String type;

    public final Consumer<KnowledgeLoadedObject> onSuccess;

    public KnowledgeGetMessage(ActorRef<T> replyTo, String type) {
        this(replyTo, type, NOP);
    }

    public KnowledgeGetMessage(ActorRef<T> replyTo, String type, Consumer<KnowledgeLoadedObject> onSuccess) {
        super(replyTo);
        this.type = type;
        this.onSuccess = onSuccess;
    }

}
