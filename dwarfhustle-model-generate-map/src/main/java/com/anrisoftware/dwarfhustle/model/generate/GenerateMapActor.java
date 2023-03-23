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

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.factory.Maps;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousExtrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousIntrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.Metamorphic;
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.SpecialStoneLayer;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.generate.GenerateMapMessage.GenerateErrorMessage;
import com.anrisoftware.dwarfhustle.model.generate.GenerateMapMessage.GenerateProgressMessage;
import com.anrisoftware.dwarfhustle.model.generate.GenerateMapMessage.GenerateSuccessMessage;
import com.anrisoftware.dwarfhustle.model.generate.WorkerBlocks.WorkerBlocksFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeGetMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.OrientDB;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.TimerScheduler;
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
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class SendGeneratorStatusMessage extends Message {

        public final GenerateMapMessage om;

        public final WorkerBlocks workerBlocks;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class WrappedKnowledgeResponse extends Message {
        private final KnowledgeResponseMessage response;
    }

    /**
     * Factory to create {@link GenerateMapActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface GenerateMapActorFactory {

        GenerateMapActor create(TimerScheduler<Message> timer, ActorContext<Message> context, OrientDB db,
                ActorRef<Message> knowledge);
    }

    public static Behavior<Message> create(Injector injector, OrientDB db, ActorRef<Message> knowledge) {
        return Behaviors.withTimers(timer -> Behaviors.setup(context -> injector
                .getInstance(GenerateMapActorFactory.class).create(timer, context, db, knowledge).start()));
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
    private TimerScheduler<Message> timer;

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
    private WorkerBlocksFactory workerBlocksFactory;

    private ActorRef<KnowledgeResponseMessage> knowledgeResponseAdapter;

    private Optional<GenerateMapMessage> generateMap;

    private Map<String, ListIterable<GameObject>> materials;

    /**
     * Initial behavior. Returns a behavior for the messages from
     * {@link #getInitialBehavior()}.
     */
    public Behavior<Message> start() {
        this.generateMap = Optional.empty();
        this.knowledgeResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class,
                WrappedKnowledgeResponse::new);
        return getInitialBehavior().build();
    }

    /**
     * Handle {@link GenerateMapMessage}. Returns a behavior for the messages from
     * {@link #getInitialBehavior()}.
     */
    protected Behavior<Message> onGenerateMap(GenerateMapMessage m) {
        log.debug("onGenerate {}", m);
        this.materials = Maps.mutable.empty();
        this.generateMap = Optional.of(m);
        knowledge.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Sedimentary.TYPE));
        knowledge.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousIntrusive.TYPE));
        knowledge.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, IgneousExtrusive.TYPE));
        knowledge.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Metamorphic.TYPE));
        knowledge.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, SpecialStoneLayer.TYPE));
        knowledge.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Soil.TYPE));
        knowledge.tell(new KnowledgeGetMessage<>(knowledgeResponseAdapter, Gas.TYPE));
        return Behaviors.same();
    }

    /**
     * Handle {@link MaterialsLoadSuccessMessage}. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}.
     */
    protected Behavior<Message> onMaterialsLoadSuccess(MaterialsLoadSuccessMessage m) {
        log.debug("onMaterialsLoadSuccess {}", m);
        var workerBlocks = workerBlocksFactory.create(db, materials, generateMap.get().p);
        new Thread(() -> {
            workerBlocks.generate(generateMap.get());
        }).run();
        timer.startTimerAtFixedRate(new SendGeneratorStatusMessage(generateMap.get(), workerBlocks),
                Duration.ofSeconds(5), Duration.ofSeconds(5));
        return Behaviors.same();
    }

    /**
     * Handle {@link SendGeneratorStatusMessage}. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}.
     */
    protected Behavior<Message> onSendGeneratorStatus(SendGeneratorStatusMessage m) {
        log.debug("onSendGeneratorStatus {}", m);
        m.om.replyTo.tell(new GenerateProgressMessage(generateMap.get(), m.workerBlocks.getBlocksDone(),
                m.workerBlocks.isGenerateDone()));
        timer.cancelAll();
        if (m.workerBlocks.isGenerateDone()) {
            generateMap.get().replyTo.tell(new GenerateSuccessMessage(generateMap.get()));
        }
        return Behaviors.same();
    }

    /**
     * Handles {@link WrappedKnowledgeResponse}. Returns a behavior for the messages
     * from {@link #getInitialBehavior()}.
     */
    private Behavior<Message> onWrappedKnowledgeResponse(WrappedKnowledgeResponse m) {
        log.debug("onWrappedKnowledgeBaseResponse {}", m);
        if (m.response instanceof KnowledgeResponseErrorMessage em) {
            log.error("Error load materials", em.error);
            generateMap.get().replyTo.tell(new GenerateErrorMessage(generateMap.get(), em.error));
            return Behaviors.stopped();
        } else if (m.response instanceof KnowledgeResponseSuccessMessage rm) {
            materials.put(rm.go.type, rm.go.objects);
            if (materials.size() == 7) {
                context.getSelf().tell(new MaterialsLoadSuccessMessage());
            }
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link GenerateMapMessage}
     * <li>{@link MaterialsLoadSuccessMessage}
     * <li>{@link SendGeneratorStatusMessage}
     * <li>{@link WrappedKnowledgeResponse}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(GenerateMapMessage.class, this::onGenerateMap)//
                .onMessage(MaterialsLoadSuccessMessage.class, this::onMaterialsLoadSuccess)//
                .onMessage(SendGeneratorStatusMessage.class, this::onSendGeneratorStatus)//
                .onMessage(WrappedKnowledgeResponse.class, this::onWrappedKnowledgeResponse)//
        ;
    }

}
