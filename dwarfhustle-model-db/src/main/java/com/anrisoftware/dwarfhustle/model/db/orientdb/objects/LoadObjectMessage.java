package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import java.util.function.Consumer;
import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage.ObjectsSuccessMessage;
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
public class LoadObjectMessage<T extends Message> extends ObjectsMessage<T> {

    @ToString(callSuper = true)
    public static class LoadObjectSuccessMessage<T extends Message>
            extends ObjectsSuccessMessage<LoadObjectMessage<T>> {

        public final GameObject go;

        public LoadObjectSuccessMessage(LoadObjectMessage<T> om, GameObject go) {
            super(om);
            this.go = go;
        }

    }

    @ToString(callSuper = true)
    public static class LoadObjectErrorMessage<T extends Message> extends ObjectsErrorMessage<LoadObjectMessage<T>> {
        public LoadObjectErrorMessage(LoadObjectMessage<T> om, Throwable error) {
            super(om, error);
        }

    }

    private static final Consumer<GameObject> EMPTY_CONSUMER = go -> {
    };

    public final String objectType;

    public final Consumer<GameObject> consumer;

    public final Function<ODatabaseDocument, OResultSet> query;

    public LoadObjectMessage(ActorRef<T> replyTo, String objectType, Function<ODatabaseDocument, OResultSet> query) {
        this(replyTo, objectType, EMPTY_CONSUMER, query);
    }

    public LoadObjectMessage(ActorRef<T> replyTo, String objectType, Consumer<GameObject> consumer,
            Function<ODatabaseDocument, OResultSet> query) {
        super(replyTo);
        this.objectType = objectType;
        this.consumer = consumer;
        this.query = query;
    }

}
