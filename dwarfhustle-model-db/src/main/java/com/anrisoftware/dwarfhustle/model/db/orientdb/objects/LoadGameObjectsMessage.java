package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import java.util.function.Consumer;
import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to load multiple {@link GameObject} from the database. Responds with
 * either {@link LoadObjectSuccessMessage} or {@link LoadObjectErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class LoadGameObjectsMessage extends AbstractLoadObjectMessage {

	private static final Consumer<GameObject> EMPTY_CONSUMER = go -> {
	};

	public final String objectType;

	public final Consumer<GameObject> consumer;

	public final Function<ODatabaseDocument, OResultSet> query;

	public LoadGameObjectsMessage(ActorRef<ObjectsResponseMessage> replyTo, String objectType,
			Function<ODatabaseDocument, OResultSet> query) {
		this(replyTo, objectType, EMPTY_CONSUMER, query);
	}

	public LoadGameObjectsMessage(ActorRef<ObjectsResponseMessage> replyTo, String objectType,
			Consumer<GameObject> consumer, Function<ODatabaseDocument, OResultSet> query) {
		super(replyTo);
		this.objectType = objectType;
		this.query = query;
		this.consumer = consumer;
	}

}
