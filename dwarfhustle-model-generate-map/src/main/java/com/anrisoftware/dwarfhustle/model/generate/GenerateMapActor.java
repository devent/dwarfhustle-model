/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.generate;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.eclipse.collections.api.map.primitive.IntObjectMap;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.generate.GenerateMapMessage.GenerateErrorMessage;
import com.anrisoftware.dwarfhustle.model.generate.WorkerBlocks.WorkerBlocksFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeGetMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeReplyMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.OrientDB;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class GenerateMapActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            GenerateMapActor.class.getSimpleName());

    public static final String NAME = GenerateMapActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class ResponseErrorMessage extends Message {

        public final GenerateMapMessage generateMessage;

        public final Throwable error;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class MaterialsLoadSuccessMessage extends Message {

        @ToString.Exclude
        public final Map<String, IntObjectMap<? extends GameObject>> materials;

    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class GenerateSuccessMessage extends Message {

        public final GenerateMapMessage generateMessage;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class WrappedKnowledgeBaseResponse extends Message {

        private final KnowledgeResponseMessage response;
    }

    /**
     * Factory to create {@link GenerateMapActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface GenerateMapActorFactory {

        GenerateMapActor create(ActorContext<Message> context, OrientDB db, ActorRef<Message> knowledge);
    }

    public static Behavior<Message> create(Injector injector, OrientDB db, ActorRef<Message> knowledge) {
        return Behaviors.setup(
                context -> injector.getInstance(GenerateMapActorFactory.class).create(context, db, knowledge).start());
    }

    /**
     * Creates the {@link GenerateMapActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout, OrientDB db,
            ActorRef<Message> knowledge) {
        var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, db, knowledge));
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    @Assisted
    private OrientDB db;

    @Inject
    @Assisted
    private ActorRef<Message> knowledge;

    @Inject
    private WorkerBlocksFactory workerActorFactory;

    private ActorRef<KnowledgeResponseMessage> knowledgeBaseResponseAdapter;

    private final Duration timeout = Duration.ofSeconds(600);

    private Optional<GenerateMapMessage> generateMap;

    /**
     * Initial behavior. Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link GenerateMapMessage}
     * <li>{@link MaterialsLoadSuccessMessage}
     * <li>{@link WrappedKnowledgeBaseResponse}
     * </ul>
     */
    public Behavior<Message> start() {
        this.generateMap = Optional.empty();
        this.knowledgeBaseResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class,
                WrappedKnowledgeBaseResponse::new);
        return getInitialBehavior().build();
    }

    /**
     * Handle {@link GenerateMapMessage}. Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link GenerateMapMessage}
     * <li>{@link MaterialsLoadSuccessMessage}
     * <li>{@link WrappedKnowledgeBaseResponse}
     * </ul>
     */
    protected Behavior<Message> onGenerateMap(GenerateMapMessage m) {
        log.debug("onGenerate {}", m);
        this.generateMap = Optional.of(m);
        knowledge.tell(new KnowledgeGetMessage<>(knowledgeBaseResponseAdapter, Sedimentary.TYPE));
        return Behaviors.same();
    }

    /**
     * Handle {@link MaterialsLoadSuccessMessage}. Returns a behavior for the
     * messages:
     *
     * <ul>
     * <li>{@link GenerateMapMessage}
     * <li>{@link MaterialsLoadSuccessMessage}
     * <li>{@link WrappedKnowledgeBaseResponse}
     * </ul>
     */
    protected Behavior<Message> onMaterialsLoadSuccess(MaterialsLoadSuccessMessage m) {
        log.debug("onMaterialsLoadSuccess {}", m);
        var workerActor = workerActorFactory.create(db);
        return Behaviors.same();
    }

    /**
     * Handles {@link WrappedKnowledgeBaseResponse}. Returns a behavior for the
     * messages:
     *
     * <ul>
     * <li>{@link GenerateMapMessage}
     * <li>{@link MaterialsLoadSuccessMessage}
     * <li>{@link WrappedKnowledgeBaseResponse}
     * </ul>
     */
    private Behavior<Message> onWrappedKnowledgeBaseResponse(WrappedKnowledgeBaseResponse m) {
        log.debug("onWrappedKnowledgeBaseResponse {}", m);
        var response = m.response;
        if (response instanceof KnowledgeErrorMessage em) {
            log.error("Error load materials", em.error);
            generateMap.get().replyTo.tell(new GenerateErrorMessage(generateMap.get(), em.error));
            return Behaviors.stopped();
        } else if (response instanceof KnowledgeReplyMessage rm) {
            // context.getSelf().tell(new MaterialsLoadSuccessMessage(rm.go));
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link GenerateMapMessage}
     * <li>{@link MaterialsLoadSuccessMessage}
     * <li>{@link WrappedKnowledgeBaseResponse}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(GenerateMapMessage.class, this::onGenerateMap)//
                .onMessage(MaterialsLoadSuccessMessage.class, this::onMaterialsLoadSuccess)//
                .onMessage(WrappedKnowledgeBaseResponse.class, this::onWrappedKnowledgeBaseResponse)//
        ;
    }

}
