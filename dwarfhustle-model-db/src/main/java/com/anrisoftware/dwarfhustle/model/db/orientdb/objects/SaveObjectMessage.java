package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsSuccessMessage;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to load a {@link GameObject} from the database. Responds with either
 * {@link LoadObjectSuccessMessage} or {@link LoadObjectErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class SaveObjectMessage<T extends Message> extends ObjectsMessage<T> {

    @ToString(callSuper = true)
    public static class SaveObjectSuccessMessage<T extends Message>
            extends ObjectsSuccessMessage<SaveObjectMessage<T>> {

        public final GameObject go;

        public SaveObjectSuccessMessage(SaveObjectMessage<T> om, GameObject go) {
            super(om);
            this.go = go;
        }

    }

    @ToString(callSuper = true)
    public static class SaveObjectErrorMessage<T extends Message> extends ObjectsErrorMessage<SaveObjectMessage<T>> {
        public SaveObjectErrorMessage(SaveObjectMessage<T> om, Throwable error) {
            super(om, error);
        }

    }

    public final GameObject go;

    public SaveObjectMessage(ActorRef<T> replyTo, GameObject go) {
        super(replyTo);
        this.go = go;
    }

}
