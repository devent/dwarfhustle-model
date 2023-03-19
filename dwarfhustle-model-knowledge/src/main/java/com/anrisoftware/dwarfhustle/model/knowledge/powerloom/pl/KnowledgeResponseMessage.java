package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import java.util.Map;

import org.eclipse.collections.api.map.primitive.IntObjectMap;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

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
    public static class KnowledgeReplyMessage extends KnowledgeResponseMessage {

        @ToString.Exclude
        public final Map<String, IntObjectMap<? extends GameObject>> go;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    public static class KnowledgeErrorMessage extends KnowledgeResponseMessage {

        @ToString.Exclude
        public final KnowledgeMessage<?> om;

        public final Exception error;
    }

}
