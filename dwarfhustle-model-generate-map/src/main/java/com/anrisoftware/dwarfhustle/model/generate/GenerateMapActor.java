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
package com.anrisoftware.dwarfhustle.model.generate;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.eclipse.collections.api.map.primitive.IntObjectMap;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.IgneousExtrusive;
import com.anrisoftware.dwarfhustle.model.api.IgneousIntrusive;
import com.anrisoftware.dwarfhustle.model.api.Material;
import com.anrisoftware.dwarfhustle.model.api.Metamorphic;
import com.anrisoftware.dwarfhustle.model.api.Sedimentary;
import com.anrisoftware.dwarfhustle.model.generate.WorkerActor.WorkerActorFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage.ResponseMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

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
	private static class ResponseErrorMessage extends ResponseMessage {

		public final Throwable error;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class MaterialsLoadSuccessMessage extends ResponseMessage {

		@ToString.Exclude
		public final Map<String, IntObjectMap<? extends Material>> materials;

		public final GenerateMessage m;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class GenerateSuccessMessage extends ResponseMessage {

		public final GenerateMessage generateMessage;
	}

	/**
	 * Factory to create {@link GenerateMapActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface GenerateMapActorFactory {

		GenerateMapActor create(ActorContext<Message> context, @Assisted("db") ActorRef<Message> db,
				@Assisted("knowledge") ActorRef<Message> knowledge);
	}

	public static Behavior<Message> create(Injector injector, ActorRef<Message> db, ActorRef<Message> knowledge) {
		return Behaviors.setup((context) -> {
			return injector.getInstance(GenerateMapActorFactory.class).create(context, db, knowledge).start();
		});
	}

	/**
	 * Creates the {@link GenerateMapActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout, ActorRef<Message> db,
			ActorRef<Message> knowledge) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, db, knowledge));
	}

	@Inject
	@Assisted
	private ActorContext<Message> context;

	@Inject
	@Assisted("db")
	private ActorRef<Message> db;

	@Inject
	@Assisted("knowledge")
	private ActorRef<Message> knowledge;

	@Inject
	private WorkerActorFactory workerActorFactory;

	private final Duration timeout = Duration.ofSeconds(300);

	/**
	 * Initial behavior. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GenerateMessage}
	 * <li>{@link MaterialsLoadSuccessMessage}
	 * <li>{@link GenerateSuccessMessage}
	 * </ul>
	 */
	public Behavior<Message> start() {
		return getInitialBehavior().build();
	}

	/**
	 * Handle {@link GenerateMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GenerateMessage}
	 * <li>{@link MaterialsLoadSuccessMessage}
	 * <li>{@link GenerateSuccessMessage}
	 * </ul>
	 */
	protected Behavior<Message> onGenerate(GenerateMessage m) {
		log.debug("onGenerate {}", m);
		retrieveMapTileMaterials(m);
		return Behaviors.same();
	}

	/**
	 * Handle {@link MaterialsLoadSuccessMessage}. Returns a behavior for the
	 * messages:
	 *
	 * <ul>
	 * <li>{@link GenerateMessage}
	 * <li>{@link MaterialsLoadSuccessMessage}
	 * <li>{@link GenerateSuccessMessage}
	 * </ul>
	 */
	protected Behavior<Message> onMaterialsLoadSuccess(MaterialsLoadSuccessMessage m) {
		log.debug("onMaterialsLoadSuccess {}", m);
		var workerActor = context.spawn(workerActorFactory.create(context, db, knowledge).start(),
				WorkerActor.NAME);
		context.ask(WorkerActor.ResponseMessage.class, workerActor, timeout,
				(ActorRef<WorkerActor.ResponseMessage> ref) -> new WorkerActor.GenerateMapMessage(ref, m.m,
						m.materials),
				(response, throwable) -> {
					if (throwable != null) {
						return new ResponseErrorMessage(throwable);
					}
					if (response instanceof WorkerActor.ErrorMessage) {
						var wm = (WorkerActor.ErrorMessage) response;
						return new ResponseErrorMessage(wm.error);
					} else {
						var wm = (WorkerActor.SuccessMessage) response;
						return new GenerateSuccessMessage(wm.originalMessage.generateMessage);
					}
				});
		return Behaviors.same();
	}

	/**
	 * Handle {@link GenerateSuccessMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GenerateMessage}
	 * <li>{@link MaterialsLoadSuccessMessage}
	 * <li>{@link GenerateSuccessMessage}
	 * </ul>
	 */
	protected Behavior<Message> onGenerateSuccess(GenerateSuccessMessage m) {
		log.debug("onGenerateSuccess {}", m);
		m.generateMessage.replyTo.tell(new GenerateMessage.GenerateSuccessMessage(m.generateMessage));
		return Behaviors.same();
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GenerateMessage}
	 * <li>{@link MaterialsLoadSuccessMessage}
	 * <li>{@link GenerateSuccessMessage}
	 * </ul>
	 */
	private BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(GenerateMessage.class, this::onGenerate)//
				.onMessage(MaterialsLoadSuccessMessage.class, this::onMaterialsLoadSuccess)//
				.onMessage(GenerateSuccessMessage.class, this::onGenerateSuccess)//
		;
	}

	private void retrieveMapTileMaterials(GenerateMessage m) {
		context.ask(KnowledgeBaseMessage.ResponseMessage.class, knowledge, timeout,
				(ActorRef<KnowledgeBaseMessage.ResponseMessage> ref) -> new KnowledgeBaseMessage.GetMessage(ref,
						Sedimentary.TYPE, IgneousIntrusive.TYPE, IgneousExtrusive.TYPE, Metamorphic.TYPE),
				(response, throwable) -> {
					if (throwable != null) {
						return new ResponseErrorMessage(throwable);
					}
					if (response instanceof KnowledgeBaseMessage.ErrorMessage) {
						var km = (KnowledgeBaseMessage.ErrorMessage) response;
						return new ResponseErrorMessage(km.error);
					} else {
						var km = (KnowledgeBaseMessage.ReplyMessage) response;
						return new MaterialsLoadSuccessMessage(km.materials, m);
					}
				});
	}

}
