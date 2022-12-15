/*
 * dwarfhustle-model-actor - Manages the compile dependencies for the model.
 * Copyright © 2022 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
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

    @SuppressWarnings("unchecked")
    @Inject
    public MessageActor(@Assisted ActorContext<Message> context) {
        super((ActorContext<T>) context);
    }

}
