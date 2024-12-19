/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.materials;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Material what stuff is made of.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class BlockMaterial extends KnowledgeObject {

    public static final int OBJECT_TYPE = BlockMaterial.class.getSimpleName().hashCode();

    public static final String TYPE = "Material";

    private String name;

    private float meltingPoint;

    private float density;

    private float specificHeatCapacity;

    private float thermalConductivity;

    public BlockMaterial(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return BlockMaterial.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return BlockMaterial.TYPE;
    }

    @Override
    public <T extends GameObject> T createObject(byte[] id) {
        return null;
    }
}
