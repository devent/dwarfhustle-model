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

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.factory.Maps;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousExtrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousIntrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.Liquid;
import com.anrisoftware.dwarfhustle.model.api.materials.Metamorphic;
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.generate.GenerateMapMessage.GenerateErrorMessage;
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
import jakarta.inject.Inject;
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
    private static class StartGenerateMessage extends Message {
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class SendGeneratorStatusMessage extends Message {

        public final GenerateMapMessage om;
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

    private ActorRef<KnowledgeResponseMessage> knowledgeResponseAdapter;

    private Optional<GenerateMapMessage> generateMap;

    private Map<String, ListIterable<KnowledgeObject>> materials;

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
        knowledge.tell(createKgMessage(Sedimentary.TYPE));
        knowledge.tell(createKgMessage(IgneousIntrusive.TYPE));
        knowledge.tell(createKgMessage(IgneousExtrusive.TYPE));
        knowledge.tell(createKgMessage(Metamorphic.TYPE));
        knowledge.tell(createKgMessage(Liquid.TYPE));
        knowledge.tell(createKgMessage(Soil.TYPE));
        knowledge.tell(createKgMessage(Gas.TYPE));
        return Behaviors.same();
    }

    private KnowledgeGetMessage<KnowledgeResponseMessage> createKgMessage(String type) {
        return new KnowledgeGetMessage<>(knowledgeResponseAdapter, type);
    }

    /**
     * Handle {@link MaterialsLoadSuccessMessage}. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}.
     */
    protected Behavior<Message> onMaterialsLoadSuccess(MaterialsLoadSuccessMessage m) {
        log.debug("onMaterialsLoadSuccess {}", m);
        // this.workerBlocks = Optional.of(workerBlocksFactory.create(db, materials,
        // generateMap.get().p));
        context.getSelf().tell(new StartGenerateMessage());
        // timer.startTimerAtFixedRate(new SendGeneratorStatusMessage(generateMap.get(),
        // workerBlocks.get()),
        // Duration.ofSeconds(5), Duration.ofSeconds(5));
        return Behaviors.same();
    }

    /**
     * Handle {@link StartGenerateMessage}. Returns a behavior for the messages from
     * {@link #getInitialBehavior()}.
     */
    protected Behavior<Message> onStartGenerate(StartGenerateMessage m) {
        log.debug("onStartGenerate {}", m);
        var thread = new Thread(() -> {
            // m.workerBlocks.generate(generateMap.get());
        });
        thread.start();
        return Behaviors.same();
    }

    /**
     * Handle {@link SendGeneratorStatusMessage}. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}.
     */
    protected Behavior<Message> onSendGeneratorStatus(SendGeneratorStatusMessage m) {
        log.debug("onSendGeneratorStatus {}", m);
//        m.om.progressTo.tell(new GenerateProgressMessage(generateMap.get(), m.workerBlocks.getBlocksDone(),
//                m.workerBlocks.isGenerateDone()));
//        if (m.workerBlocks.isGenerateDone()) {
//            timer.cancelAll();
//            generateMap.get().replyTo.tell(new GenerateSuccessMessage(generateMap.get()));
//        }
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
            // materials.put(rm.go.type, rm.go.objects);
            if (materials.size() == 7) {
                context.getSelf().tell(new MaterialsLoadSuccessMessage());
            }
        }
        return Behaviors.same();
    }

    /**
     * Handles {@link ShutdownMessage}.
     */
    private Behavior<Message> onShutdown(ShutdownMessage m) {
        log.debug("onShutdown {}", m);
//        workerBlocks.ifPresent(WorkerBlocks::cancel);
        return Behaviors.stopped();
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link GenerateMapMessage}
     * <li>{@link MaterialsLoadSuccessMessage}
     * <li>{@link SendGeneratorStatusMessage}
     * <li>{@link StartGenerateMessage}
     * <li>{@link ShutdownMessage}
     * <li>{@link WrappedKnowledgeResponse}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(GenerateMapMessage.class, this::onGenerateMap)//
                .onMessage(MaterialsLoadSuccessMessage.class, this::onMaterialsLoadSuccess)//
                .onMessage(SendGeneratorStatusMessage.class, this::onSendGeneratorStatus)//
                .onMessage(StartGenerateMessage.class, this::onStartGenerate)//
                .onMessage(WrappedKnowledgeResponse.class, this::onWrappedKnowledgeResponse)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
        ;
    }

}
