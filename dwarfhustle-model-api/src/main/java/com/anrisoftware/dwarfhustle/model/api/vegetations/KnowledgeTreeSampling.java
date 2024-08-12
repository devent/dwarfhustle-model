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

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Sampling of the tree.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeTreeSampling extends KnowledgeTree {

    public static final int OBJECT_TYPE = KnowledgeTreeSampling.class.getSimpleName().hashCode();

    public static final String TYPE = "Tree-Sampling";

    public KnowledgeTreeSampling(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return KnowledgeTreeSampling.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeTreeSampling.TYPE;
    }

    @Override
    public GameObject createObject(byte[] id) {
        return new TreeSampling(id);
    }
}
