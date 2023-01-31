package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to load the {@link WorldMap} from the database. Responds with either
 * {@link LoadObjectSuccessMessage} or {@link LoadObjectErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class LoadWorldMapMessage extends AbstractLoadObjectMessage {

	public LoadWorldMapMessage(ActorRef<ObjectsResponseMessage> replyTo) {
		super(replyTo);
	}

}
