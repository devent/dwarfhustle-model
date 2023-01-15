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
import com.anrisoftware.dwarfhustle.model.api.Topsoil;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage.GetMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage.GetReplyMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseMessage.ReplyMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeCommandResponseMessage.KnowledgeCommandErrorMessage;
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
		return AskPattern.ask(knowledge, replyTo -> new KnowledgeCommandReplyMessage(replyTo, () -> {
			retrieveMaterials();
			return null;
		}), timeout, context.getSystem().scheduler());
	}

	private static void retrieveMaterials() {
		metal = retrieveMaterials("Metal", Metal.class);
		metalOre = retrieveMaterials("Metal-Ore", MetalOre.class);
		metalAlloy = retrieveMaterials("Metal-Alloy", MetalAlloy.class);
		topsoil = retrieveMaterials("Topsoil", Topsoil.class);
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
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * </ul>
	 */
	private Behavior<Message> onInitialState(InitialStateMessage m) {
		log.debug("onInitialState");
		return buffer.unstashAll(getInitialBehavior()//
				.build());
	}

	/**
	 * Reacts to {@link GetReplyMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * </ul>
	 */
	private Behavior<Message> onGetReply(GetReplyMessage m) {
		log.debug("onGetReply {}", m);
		MutableMap<String, IntObjectMap<? extends Material>> map = Maps.mutable.withInitialCapacity(m.material.length);
		for (String material : m.material) {
			map.put(material, materials.get(material));
		}
		m.replyTo.tell(new ReplyMessage(map.asUnmodifiable()));
		return Behaviors.same();
	}

	/**
	 * Reacts to {@link GetMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * </ul>
	 */
	private Behavior<Message> onGet(GetMessage m) {
		log.debug("onGet {}", m);
		MutableMap<String, IntObjectMap<? extends Material>> map = Maps.mutable.withInitialCapacity(m.material.length);
		for (String material : m.material) {
			map.put(material, materials.get(material));
		}
		m.caller.tell(new ReplyMessage(map.asUnmodifiable()));
		return Behaviors.same();
	}

	private BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(GetReplyMessage.class, this::onGetReply)//
				.onMessage(GetMessage.class, this::onGet)//
		;
	}

}
