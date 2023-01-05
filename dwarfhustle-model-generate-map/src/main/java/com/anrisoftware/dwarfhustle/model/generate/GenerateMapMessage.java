package com.anrisoftware.dwarfhustle.model.generate;

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
public class GenerateMapMessage extends Message {

	@RequiredArgsConstructor
	@ToString(callSuper = true)
    public static class GenerateResponseMessage extends Message {

    }

	@RequiredArgsConstructor
	@ToString(callSuper = true)
    public static class GenerateErrorMessage extends GenerateResponseMessage {

        public final GenerateMapMessage originalMessage;

		public final Throwable error;
    }

	@RequiredArgsConstructor
	@ToString(callSuper = true)
    public static class GenerateSuccessMessage extends GenerateResponseMessage {

        public final GenerateMapMessage originalMessage;
    }

    public final ActorRef<GenerateResponseMessage> replyTo;

	public final int mapid;

	public final int width;

	public final int height;

	public final int depth;

	public int getSize() {
		return depth * height * width;
	}
}
