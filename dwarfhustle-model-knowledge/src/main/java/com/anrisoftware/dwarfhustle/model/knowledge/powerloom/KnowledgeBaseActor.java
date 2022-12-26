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
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.PowerLoomKnowledgeActor.WORKING_MODULE;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.Clay;
import com.anrisoftware.dwarfhustle.model.api.IgneousExtrusive;
import com.anrisoftware.dwarfhustle.model.api.IgneousIntrusive;
import com.anrisoftware.dwarfhustle.model.api.Material;
import com.anrisoftware.dwarfhustle.model.api.Metal;
import com.anrisoftware.dwarfhustle.model.api.MetalAlloy;
import com.anrisoftware.dwarfhustle.model.api.MetalOre;
import com.anrisoftware.dwarfhustle.model.api.Metamorphic;
import com.anrisoftware.dwarfhustle.model.api.Sand;
import com.anrisoftware.dwarfhustle.model.api.Seabed;
import com.anrisoftware.dwarfhustle.model.api.Sedimentary;
import com.anrisoftware.dwarfhustle.model.api.TopSoil;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage.GetMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage.ReplyMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeCommandMessage.KnowledgeCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeCommandMessage.KnowledgeCommandResponseMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.stella.FloatWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides specific knowledge.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class KnowledgeBaseActor {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
			KnowledgeBaseActor.class.getSimpleName());

	public static final String NAME = KnowledgeBaseActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	public static Map<String, IntObjectMap<Material>> materials;

	public static IntObjectMap<Material> metal;

	public static IntObjectMap<Material> metalOre;

	public static IntObjectMap<Material> metalAlloy;

	public static IntObjectMap<Material> topsoil;

	public static IntObjectMap<Material> seabed;

	public static IntObjectMap<Material> sand;

	public static IntObjectMap<Material> clay;

	public static IntObjectMap<Material> sedimentary;

	public static IntObjectMap<Material> igneousIntrusive;

	public static IntObjectMap<Material> igneousExtrusive;

	public static IntObjectMap<Material> metamorphic;

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
	 * Factory to create {@link KnowledgeBaseActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface KnowledgeBaseActorFactory {

		KnowledgeBaseActor create(ActorContext<Message> context, StashBuffer<Message> stash);
	}

	public static Behavior<Message> create(Injector injector, ActorRef<Message> knowledge) {
		return Behaviors.withStash(100, stash -> Behaviors.setup((context) -> {
			loadKnowledgeBase(context, knowledge);
			return injector.getInstance(KnowledgeBaseActorFactory.class).create(context, stash).start();
		}));
	}

	private static void loadKnowledgeBase(ActorContext<Message> context, ActorRef<Message> knowledge) {
		context.pipeToSelf(loadKnowledgeBase0(context, knowledge), (result, cause) -> {
			if (cause == null) {
				if (result instanceof KnowledgeCommandErrorMessage) {
					var m = (KnowledgeCommandErrorMessage) result;
					return new SetupErrorMessage(m.error);
				}
				return new InitialStateMessage();
			} else {
				return new SetupErrorMessage(cause);
			}
		});
	}

	private static CompletionStage<KnowledgeCommandResponseMessage> loadKnowledgeBase0(ActorContext<Message> context,
			ActorRef<Message> knowledge) {
		Duration timeout = Duration.ofSeconds(30);
		return AskPattern.ask(knowledge, replyTo -> new KnowledgeCommandMessage(replyTo, () -> {
			retrieveMaterials();
			return null;
		}), timeout, context.getSystem().scheduler());
	}

	private static void retrieveMaterials() {
		metal = retrieveMaterials("Metal", Metal.class);
		metalOre = retrieveMaterials("Metal-Ore", MetalOre.class);
		metalAlloy = retrieveMaterials("Metal-Alloy", MetalAlloy.class);
		topsoil = retrieveMaterials("Topsoil", TopSoil.class);
		seabed = retrieveMaterials("Seabed", Seabed.class);
		sand = retrieveMaterials("Sand", Sand.class);
		clay = retrieveMaterials("Clay", Clay.class);
		sedimentary = retrieveMaterials("Sedimentary", Sedimentary.class);
		igneousIntrusive = retrieveMaterials("Igneous-Intrusive", IgneousIntrusive.class);
		igneousExtrusive = retrieveMaterials("Igneous-Extrusive", IgneousExtrusive.class);
		metamorphic = retrieveMaterials("Metamorphic", Metamorphic.class);
		MutableMap<String, IntObjectMap<Material>> mmaterials = Maps.mutable.empty();
		mmaterials.put("Metal", metal);
		mmaterials.put("Metal-Ore", metalOre);
		mmaterials.put("Metal-Alloy", metalAlloy);
		mmaterials.put("Topsoil", topsoil);
		mmaterials.put("Seabed", seabed);
		mmaterials.put("Sand", sand);
		mmaterials.put("Clay", clay);
		mmaterials.put("Sedimentary", sedimentary);
		mmaterials.put("Igneous-Intrusive", igneousIntrusive);
		mmaterials.put("Igneous-Extrusive", igneousExtrusive);
		mmaterials.put("Metamorphic", metamorphic);
		materials = mmaterials.asUnmodifiable();
	}

	@SneakyThrows
	private static IntObjectMap<Material> retrieveMaterials(String type, Class<? extends Material> matType) {
		var answer = PLI.sRetrieve("all (" + type + " ?x)", WORKING_MODULE, null);
		MutableIntObjectMap<Material> map = IntObjectMaps.mutable.empty();
		LogicObject next;
		while ((next = (LogicObject) answer.pop()) != null) {
			int id = next.surrogateValueInverse.symbolId;
			String name = next.surrogateValueInverse.symbolName;
			float meltingPoint = retrieveFloat("melting-point-material", name);
			float density = retrieveFloat("density-of-material", name);
			float shc = retrieveFloat("specific-heat-capacity-of-material", name);
			float tc = retrieveFloat("thermal-conductivity-of-material", name);
			var material = matType.getConstructors()[0].newInstance(id, name, meltingPoint, density, shc, tc);
			map.put(id, (Material) material);
		}
		log.trace("retrieveMaterials {} {}", type, map.size());
		return map.asUnmodifiable();
	}

	private static float retrieveFloat(String function, String name) {
		var buff = new StringBuilder();
		buff.append("?x (");
		buff.append(function);
		buff.append(" ");
		buff.append(name);
		buff.append(" ?x)");
		var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
		FloatWrapper next;
		while ((next = (FloatWrapper) answer.pop()) != null) {
			return (float) next.wrapperValue;
		}
		return -1;
	}

	/**
	 * Creates the {@link KnowledgeBaseActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
			ActorRef<Message> knowledge) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, knowledge));
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
	 * <li>{@link KnowledgeCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onInitialState(InitialStateMessage m) {
		log.debug("onInitialState");
		return buffer.unstashAll(getInitialBehavior()//
				.build());
	}

	/**
	 * Reacts to {@link GetMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GetMessage}
	 * </ul>
	 */
	private Behavior<Message> onGet(GetMessage m) {
		log.debug("onGet {}", m);
		m.replyTo.tell(new ReplyMessage(materials.get(m.material)));
		return Behaviors.same();
	}

	private BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(GetMessage.class, this::onGet)//
		;
	}

}
