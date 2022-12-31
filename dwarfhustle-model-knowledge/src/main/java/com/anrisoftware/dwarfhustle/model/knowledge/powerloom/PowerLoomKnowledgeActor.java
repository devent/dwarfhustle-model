/*
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 * Released as open-source under the Apache License, Version 2.0.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model
 * ****************************************************************************
 *
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model is a derivative work based on Josua Tippetts' C++ library:
 * http://accidentalnoise.sourceforge.net/index.html
 * ****************************************************************************
 *
 * Copyright (C) 2011 Joshua Tippetts
 *
 *   This software is provided 'as-is', without any express or implied
 *   warranty.  In no event will the authors be held liable for any damages
 *   arising from the use of this software.
 *
 *   Permission is granted to anyone to use this software for any purpose,
 *   including commercial applications, and to alter it and redistribute it
 *   freely, subject to the following restrictions:
 *
 *   1. The origin of this software must not be misrepresented; you must not
 *      claim that you wrote the original software. If you use this software
 *      in a product, an acknowledgment in the product documentation would be
 *      appreciated but is not required.
 *   2. Altered source versions must be plainly marked as such, and must not be
 *      misrepresented as being the original software.
 *   3. This notice may not be removed or altered from any source distribution.
 *
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model bundles and uses the RandomCL library:
 * https://github.com/bstatcomp/RandomCL
 * ****************************************************************************
 *
 * BSD 3-Clause License
 *
 * Copyright (c) 2018, Tadej Ciglarič, Erik Štrumbelj, Rok Češnovar. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
