package com.anrisoftware.dwarfhustle.model.knowledge.generate;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to generate game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class GenerateMessage extends Message {

	@RequiredArgsConstructor
	@ToString(callSuper = true)
    public static class GenerateResponseMessage extends Message {

    }

	@RequiredArgsConstructor
	@ToString(callSuper = true)
    public static class GenerateErrorMessage extends GenerateResponseMessage {

        public final GenerateMessage originalMessage;

        public final Exception error;
    }

	@RequiredArgsConstructor
	@ToString(callSuper = true)
    public static class GenerateSuccessMessage extends GenerateResponseMessage {

        public final GenerateMessage originalMessage;
    }

    public final ActorRef<GenerateResponseMessage> replyTo;

	public final int mapid;

	public final int width;

	public final int height;

	public final int depth;
}
