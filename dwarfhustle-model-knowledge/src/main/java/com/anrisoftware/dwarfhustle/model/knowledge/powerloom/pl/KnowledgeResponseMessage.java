/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeLoadedObject;

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
