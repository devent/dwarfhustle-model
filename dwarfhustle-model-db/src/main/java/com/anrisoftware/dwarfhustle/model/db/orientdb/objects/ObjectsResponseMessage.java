package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Objects response message.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
@RequiredArgsConstructor
public class ObjectsResponseMessage<T extends ObjectsMessage<?>> extends Message {

    /**
     * Objects success response.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class ObjectsSuccessMessage<T extends ObjectsMessage<?>> extends ObjectsResponseMessage<T> {
        public ObjectsSuccessMessage(T om) {
            super(om);
        }

    }

    /**
     * Objects error response.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @ToString
    public static class ObjectsErrorMessage<T extends ObjectsMessage<?>> extends ObjectsResponseMessage<T> {
        public final Throwable error;

        public ObjectsErrorMessage(T om, Throwable error) {
            super(om);
            this.error = error;
        }

    }

    public final T om;
}
