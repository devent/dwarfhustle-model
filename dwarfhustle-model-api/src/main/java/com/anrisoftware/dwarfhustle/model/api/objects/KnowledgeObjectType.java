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
package com.anrisoftware.dwarfhustle.model.api.objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

/**
 * Knowledge object type is the base class of all physically real objects and
 * buildings.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public abstract class KnowledgeObjectType extends KnowledgeObject implements GameMapObjectProperties {

    public static final String TYPE = "ObjectType";

    public static final int OBJECT_TYPE = TYPE.hashCode();

    private PropertiesSet p = new PropertiesSet();

    public KnowledgeObjectType(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return KnowledgeObjectType.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeObjectType.TYPE;
    }

    @Override
    protected void setupObject(GameObject go) {
        super.setupObject(go);
        val o = (GameMapObject) go;
        o.setCanSelect(isCanSelect());
        o.setElevated(isElevated());
        o.setForbidden(isForbidden());
        o.setHaveModel(isHaveModel());
        o.setHaveTex(isHaveTex());
        o.setHidden(isHidden());
    }
}
