/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.orientdb.schemas;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link MapBlock}.
 *
 * @see GameMapObjectSchema
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlockSchema implements GameObjectSchema {

    public static final String MATERIAL_FIELD = "material";

    public static final String OBJECT_FIELD = "object";

    public static final String PROPERTIES_FIELD = "p";

    public static final String CHUNK_FIELD = "chunk";

    @Override
    public void createSchema(Object db) {
        var odb = (ODatabaseDocument) db;
        var c = odb.createClass(MapBlock.OBJECT_TYPE, GameMapObject.OBJECT_TYPE);
        c.createProperty(MATERIAL_FIELD, OType.LONG);
        c.createProperty(OBJECT_FIELD, OType.LONG);
        c.createProperty(PROPERTIES_FIELD, OType.INTEGER);
        c.createProperty(CHUNK_FIELD, OType.LONG);
        new NeighboringSchema().createSchema(db, c);
        new CenterExtentSchema().createSchema(db, c);
    }

}
