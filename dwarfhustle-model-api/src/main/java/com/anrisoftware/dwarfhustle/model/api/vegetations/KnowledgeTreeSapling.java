/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.vegetations;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Sapling of the tree.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class KnowledgeTreeSapling extends KnowledgeVegetation {

    public static final String TYPE = "Tree-Sapling";

    public static final int OBJECT_TYPE = TYPE.hashCode();

    /**
     * The name of the tree that his sampling grows into.
     */
    public String growsInto;

    public KnowledgeTreeSapling(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T createObject(byte[] id) {
        var go = new TreeSapling(id);
        go.setVisible(true);
        go.setHaveModel(false);
        go.setHaveTex(true);
        return (T) go;
    }
}
