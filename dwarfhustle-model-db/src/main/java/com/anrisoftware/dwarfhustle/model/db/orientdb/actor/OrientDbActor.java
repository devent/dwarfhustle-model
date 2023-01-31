/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateDbMessage.DbAlreadyExistMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandSuccessMessage.DbCommandErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DeleteDbMessage.DbNotExistMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class OrientDbActor {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class, OrientDbActor.class.getSimpleName());

	public static final String NAME = OrientDbActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	/**
	 * Factory to create {@link OrientDbActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface OrientDbActorFactory {

		OrientDbActor create(ActorContext<Message> context);
	}

	public static Behavior<Message> create(Injector injector) {
		return Behaviors.setup((context) -> {
			return injector.getInstance(OrientDbActorFactory.class).create(context).start();
		});
	}

	/**
	 * Creates the {@link OrientDbActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector));
	}

	@Inject
	@Assisted
	private ActorContext<Message> context;

	private Optional<OrientDB> orientdb;

	private String user;

	private String password;

	private String database;

	/**
	 * Initial behavior. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link ConnectDbRemoteMessage}
	 * <li>{@link ConnectDbEmbeddedMessage}
	 * </ul>
	 */
	public Behavior<Message> start() {
		this.orientdb = Optional.empty();
		return getInitialBehavior().build();
	}

	/**
	 * Reacts to {@link ConnectDbRemoteMessage}. Replies with the
	 * {@link ConnectDbSuccessMessage} on success and with {@link DbErrorMessage} on
	 * failure. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CloseDbMessage}
	 * <li>{@link CreateDbMessage}
	 * <li>{@link DeleteDbMessage}
	 * <li>{@link DbCommandReplyMessage}
	 * <li>{@link DbCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onConnectDbRemote(ConnectDbRemoteMessage m) {
		log.debug("onConnectDbRemote {}", m);
		try {
			this.user = m.user;
			this.password = m.password;
			this.database = m.database;
			var config = OrientDBConfig.defaultConfig();
			this.orientdb = Optional.of(new OrientDB(m.url, m.user, m.password, config));
		} catch (Exception e) {
			m.replyTo.tell(new DbErrorMessage(m, e));
			return Behaviors.same();
		}
		m.replyTo.tell(new ConnectDbSuccessMessage(m, orientdb.get()));
		return getConnectedBehavior().build();
	}

	/**
	 * Reacts to {@link ConnectDbEmbeddedMessage}. Replies with the
	 * {@link ConnectDbSuccessMessage} on success and with {@link DbErrorMessage} on
	 * failure. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CloseDbMessage}
	 * <li>{@link CreateDbMessage}
	 * <li>{@link DeleteDbMessage}
	 * <li>{@link DbCommandReplyMessage}
	 * <li>{@link DbCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onConnectDbEmbedded(ConnectDbEmbeddedMessage m) {
		log.debug("onConnectDbEmbedded {}", m);
		try {
			this.user = m.user;
			this.password = m.password;
			this.database = m.database;
			this.orientdb = Optional.of(m.server.getContext());
		} catch (Exception e) {
			m.replyTo.tell(new DbErrorMessage(m, e));
			return Behaviors.same();
		}
		m.replyTo.tell(new ConnectDbSuccessMessage(m, orientdb.get()));
		return getConnectedBehavior().build();
	}

	/**
	 * Reacts to {@link CloseDbMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link ConnectDbRemoteMessage}
	 * </ul>
	 */
	private Behavior<Message> onCloseDb(CloseDbMessage m) {
		log.debug("onCloseDb {}", m);
		orientdb.ifPresent(o -> {
			o.close();
			m.replyTo.tell(new DbSuccessMessage(m));
		});
		return getInitialBehavior().build();
	}

	/**
	 * Reacts to {@link ConnectDbRemoteMessage}. Replies with
	 * {@link DbSuccessMessage} on success, with {@link DbAlreadyExistMessage} if
	 * the database already exist or with {@link DbErrorMessage} on failure. Returns
	 * a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CloseDbMessage}
	 * <li>{@link CreateDbMessage}
	 * <li>{@link DeleteDbMessage}
	 * <li>{@link DbCommandReplyMessage}
	 * <li>{@link DbCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onCreateDb(CreateDbMessage m) {
		log.debug("onCreateDb {}", m);
		try {
			if (orientdb.get().exists(database)) {
				m.replyTo.tell(new DbAlreadyExistMessage(m));
			} else {
				orientdb.get().create(database, m.type);
				m.replyTo.tell(new DbSuccessMessage(m));
			}
		} catch (Exception e) {
			m.replyTo.tell(new DbErrorMessage(m, e));
			return Behaviors.same();
		}
		return Behaviors.same();
	}

	/**
	 * Reacts to {@link DeleteDbMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CloseDbMessage}
	 * <li>{@link CreateDbMessage}
	 * <li>{@link DeleteDbMessage}
	 * <li>{@link DbCommandReplyMessage}
	 * <li>{@link DbCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onDeleteDb(DeleteDbMessage m) {
		log.debug("onDeleteDb {}", m);
		try {
			if (!orientdb.get().exists(database)) {
				m.replyTo.tell(new DbNotExistMessage(m));
			} else {
				orientdb.get().drop(database);
				m.replyTo.tell(new DbSuccessMessage(m));
			}
		} catch (Exception e) {
			m.replyTo.tell(new DbErrorMessage(m, e));
			return Behaviors.same();
		}
		return Behaviors.same();
	}

	/**
	 * Reacts to {@link DbCommandReplyMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CloseDbMessage}
	 * <li>{@link CreateDbMessage}
	 * <li>{@link DeleteDbMessage}
	 * <li>{@link DbCommandReplyMessage}
	 * <li>{@link DbCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onDbReplyCommand(DbCommandReplyMessage m) {
		log.debug("onDbReplyCommand {}", m);
		try {
			Object ret = null;
			try (var db = orientdb.get().open(database, user, password)) {
				ret = m.command.apply(db);
			}
			m.replyTo.tell(new DbCommandSuccessMessage(m, ret));
		} catch (Exception e) {
			log.error("onDbReplyCommand", e);
			m.replyTo.tell(new DbCommandErrorMessage(m, e, m.onError.apply(e)));
		}
		return Behaviors.same();
	}

	/**
	 * Reacts to {@link DbCommandReplyMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CloseDbMessage}
	 * <li>{@link CreateDbMessage}
	 * <li>{@link DeleteDbMessage}
	 * <li>{@link DbCommandReplyMessage}
	 * <li>{@link DbCommandMessage}
	 * </ul>
	 */
	private Behavior<Message> onDbCommand(DbCommandMessage m) {
		log.debug("onDbCommand {}", m);
		try {
			Object ret = null;
			try (var db = orientdb.get().open(database, user, password)) {
				ret = m.command.apply(db);
			}
			m.caller.tell(new DbCommandSuccessMessage(m, ret));
		} catch (Exception e) {
			m.caller.tell(new DbCommandErrorMessage(m, e, m.onError.apply(e)));
		}
		return Behaviors.same();
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link ConnectDbRemoteMessage}
	 * <li>{@link ConnectDbEmbeddedMessage}
	 * </ul>
	 */
	private BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(ConnectDbRemoteMessage.class, this::onConnectDbRemote)//
				.onMessage(ConnectDbEmbeddedMessage.class, this::onConnectDbEmbedded)//
		;
	}

	private BehaviorBuilder<Message> getConnectedBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(CloseDbMessage.class, this::onCloseDb)//
				.onMessage(CreateDbMessage.class, this::onCreateDb)//
				.onMessage(DeleteDbMessage.class, this::onDeleteDb)//
				.onMessage(DbCommandReplyMessage.class, this::onDbReplyCommand)//
				.onMessage(DbCommandMessage.class, this::onDbCommand)//
		;
	}

}
