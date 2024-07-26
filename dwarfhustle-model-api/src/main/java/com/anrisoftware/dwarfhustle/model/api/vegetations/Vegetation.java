/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.api.vegetations;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Grasses, shrubs, trees.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Vegetation extends GameMapObject {

    /**
     * {@link KnowledgeVegetation} RID.
     */
    public int kid;

    /**
     * Status of growth from 0.0 (seedling) to 1.0 (adult plant).
     */
    public float growth;

    public Vegetation(long id) {
        super(id);
    }

    public Vegetation(byte[] idbuf) {
        super(idbuf);
    }

    public Vegetation(long id, GameBlockPos pos) {
        super(id, pos);
    }

    public Vegetation(byte[] idbuf, GameBlockPos pos) {
        super(idbuf, pos);
    }

}
