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
package com.anrisoftware.dwarfhustle.model.api.materials;

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
public class Material extends KnowledgeObject {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = Material.class.getSimpleName();

    public static final String TYPE = "Material";

    private String name;

    private float meltingPoint;

    private float density;

    private float specificHeatCapacity;

    private float thermalConductivity;

    public Material(int kid) {
        super(kid);
    }

    @Override
    public String getObjectType() {
        return Material.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return Material.TYPE;
    }
}
