package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to load a {@link GameObject} from the database. Responds with either
 * {@link LoadObjectSuccessMessage} or {@link LoadObjectErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class LoadGameObjectMessage extends AbstractLoadObjectMessage {

	public final String objectType;

	public final Function<ODatabaseDocument, OResultSet> query;

	public LoadGameObjectMessage(ActorRef<ObjectsResponseMessage> replyTo, String objectType,
			Function<ODatabaseDocument, OResultSet> query) {
		super(replyTo);
		this.objectType = objectType;
		this.query = query;
	}

}
