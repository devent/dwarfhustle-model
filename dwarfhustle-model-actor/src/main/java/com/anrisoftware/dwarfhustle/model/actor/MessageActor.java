/*
 * dwarfhustle-model-actor - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.actor;

import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Actor for {@link Message}.
 *
 * @author Erwin Müller {@literal <erwin@mullerlpublic.de}
 */
public abstract class MessageActor<T extends MessageActor.Message> extends AbstractBehavior<T> {

    /**
     * Actor message.
     *
     * @author Erwin Müller {@literal <erwin@mullerlpublic.de}
     */
    @ToString
    public static class Message {
    }

	/**
	 * Actor message that contains the caller. The message can only be used by
	 * {@code tell()}.
	 *
	 * @author Erwin Müller {@literal <erwin@mullerlpublic.de}
	 */
	@RequiredArgsConstructor
	@ToString
	public static class CallerMessage extends Message {

		public final ActorRef<Message> caller;
	}

	/**
	 * Actor message that contains the reply to. The message is used by
	 * {@link AskPattern#ask(akka.actor.typed.RecipientRef, akka.japi.function.Function, java.time.Duration, akka.actor.typed.Scheduler)}
	 *
	 * @author Erwin Müller {@literal <erwin@mullerlpublic.de}
	 */
	@RequiredArgsConstructor
	@ToString
	public static class ReplyMessage<T> extends Message {

		public final ActorRef<T> replyTo;
	}

    @SuppressWarnings("unchecked")
    @Inject
    public MessageActor(@Assisted ActorContext<Message> context) {
        super((ActorContext<T>) context);
    }

}
