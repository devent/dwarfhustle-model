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
package com.anrisoftware.dwarfhustle.model.api.map;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Roof type.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class RoofType extends ObjectType {

    public static final int OBJECT_TYPE = RoofType.class.getSimpleName().hashCode();

    public static final String TYPE = "RoofType";

    public RoofType(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return RoofType.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return RoofType.TYPE;
    }

    @Override
    public <T extends GameObject> T createObject(byte[] id) {
        return null;
    }
}
