/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages;

import com.anrisoftware.dwarfhustle.model.api.materials.Seabed;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.google.auto.service.AutoService;

/**
 * The bottom of the ocean. All floors of the ocean are known as seabeds.
 *
 * @see Seabed
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@AutoService(GameObjectKnowledge.class)
public class SeabedStorage extends SoilStorage {

    @Override
    public String getType() {
        return Seabed.TYPE;
    }

    @Override
    public KnowledgeObject create() {
        return new Seabed();
    }
}
