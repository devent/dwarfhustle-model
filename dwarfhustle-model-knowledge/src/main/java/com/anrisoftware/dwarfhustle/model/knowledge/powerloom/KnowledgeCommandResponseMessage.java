package com.anrisoftware.dwarfhustle.model.knowledge.powerloom;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

public class KnowledgeCommandResponseMessage extends Message {

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class KnowledgeCommandErrorMessage extends KnowledgeCommandResponseMessage {

		public final Message om;

		public final Exception error;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class KnowledgeCommandSuccessMessage extends KnowledgeCommandResponseMessage {

		public final Message om;

		public final Object result;
	}

}
