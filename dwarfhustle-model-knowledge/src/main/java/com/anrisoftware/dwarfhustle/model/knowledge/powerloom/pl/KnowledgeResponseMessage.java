package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Knowledge response message.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public abstract class KnowledgeResponseMessage extends Message {

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    public static class KnowledgeResponseSuccessMessage extends KnowledgeResponseMessage {

        public final KnowledgeLoadedObject go;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    public static class KnowledgeResponseErrorMessage extends KnowledgeResponseMessage {

        @ToString.Exclude
        public final KnowledgeMessage<?> om;

        public final Exception error;
    }

}
