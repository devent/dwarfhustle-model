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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;
import static edu.isi.stella.InputStringStream.newInputStringStream;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeCommandResponseMessage.KnowledgeCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeCommandResponseMessage.KnowledgeCommandSuccessMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import edu.isi.powerloom.PLI;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class PowerLoomKnowledgeActor {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
			PowerLoomKnowledgeActor.class.getSimpleName());

	public static final String NAME = PowerLoomKnowledgeActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	public static final String WORKING_MODULE = "DWARFHUSTLE-WORKING";

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class InitialStateMessage extends Message {
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class SetupErrorMessage extends Message {

		public final Throwable cause;
	}

	/**
	 * Factory to create {@link PowerLoomKnowledgeActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface PowerLoomKnowledgeActorFactory {

		PowerLoomKnowledgeActor create(ActorContext<Message> context, StashBuffer<Message> stash);
	}

	public static Behavior<Message> create(Injector injector) {
		return Behaviors.withStash(100, stash -> Behaviors.setup((context) -> {
			loadKnowledgeBase(injector, context);
			return injector.getInstance(PowerLoomKnowledgeActorFactory.class).create(context, stash).start();
		}));
	}

	private static void loadKnowledgeBase(Injector injector, ActorContext<Message> context) {
		context.pipeToSelf(loadKnowledgeBase0(injector), (result, cause) -> {
			if (cause == null) {
				return new InitialStateMessage();
			} else {
				return new SetupErrorMessage(cause);
			}
		});
	}

	private static CompletionStage<Boolean> loadKnowledgeBase0(Injector injector) {
		return CompletableFuture.supplyAsync(() -> {
			var resources = new ArrayList<InputStream>();
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("game-map.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-sedimentary-stones.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-igneous-intrusive-stones.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-igneous-extrusive-stones.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-metamorphic-stones.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-metals.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-metals-ores.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-metals-alloys.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-clays.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-sands.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-seabeds.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("materials-topsoils.plm"));
			resources.add(PowerLoomKnowledgeActor.class.getResourceAsStream("working.plm"));
			PLI.initialize();
			for (InputStream res : resources) {
				try {
					PLI.loadStream(newInputStringStream(IOUtils.toString(res, UTF_8)), null);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return true;
		});
	}

	/**
	 * Creates the {@link PowerLoomKnowledgeActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector));
	}

	@Inject
	@Assisted
	private ActorContext<Message> context;

	@Inject
	@Assisted
	private StashBuffer<Message> buffer;

	/**
	 * Stash behavior. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link InitialStateMessage}
	 * <li>{@link Message}
	 * </ul>
	 */
	public Behavior<Message> start() {
		return Behaviors.receive(Message.class)//
				.onMessage(InitialStateMessage.class, this::onInitialState)//
				.onMessage(Message.class, this::stashOtherCommand)//
				.build();
	}

	private Behavior<Message> stashOtherCommand(Message m) {
		log.debug("stashOtherCommand: {}", m);
		buffer.stash(m);
		return Behaviors.same();
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link KnowledgeCommandReplyMessage}
	 * <li>{@link KnowledgeCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onInitialState(InitialStateMessage m) {
		log.debug("onInitialState");
		return buffer.unstashAll(getInitialBehavior()//
				.build());
	}

	/**
	 * Reacts to {@link KnowledgeCommandReplyMessage}. Returns a behavior for the
	 * messages:
	 *
	 * <ul>
	 * <li>{@link KnowledgeCommandReplyMessage}
	 * <li>{@link KnowledgeCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onKnowledgeReplyCommand(KnowledgeCommandReplyMessage m) {
		log.debug("onKnowledgeReplyCommand {}", m);
		try {
			var res = m.command.get();
			m.replyTo.tell(new KnowledgeCommandSuccessMessage(m, res));
		} catch (Exception e) {
			m.replyTo.tell(new KnowledgeCommandErrorMessage(m, e));
		}
		return Behaviors.same();
	}

	/**
	 * Reacts to {@link KnowledgeCommandMessage}. Returns a behavior for the
	 * messages:
	 *
	 * <ul>
	 * <li>{@link KnowledgeCommandReplyMessage}
	 * <li>{@link KnowledgeCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onKnowledgeCommand(KnowledgeCommandMessage m) {
		log.debug("onKnowledgeCommand {}", m);
		try {
			var res = m.command.get();
			m.caller.tell(new KnowledgeCommandSuccessMessage(m, res));
		} catch (Exception e) {
			m.caller.tell(new KnowledgeCommandErrorMessage(m, e));
		}
		return Behaviors.same();
	}

	private BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(KnowledgeCommandReplyMessage.class, this::onKnowledgeReplyCommand)//
				.onMessage(KnowledgeCommandMessage.class, this::onKnowledgeCommand)//
		;
	}

}
