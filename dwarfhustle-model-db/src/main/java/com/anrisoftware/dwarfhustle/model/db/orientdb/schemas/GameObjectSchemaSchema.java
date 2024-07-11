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

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link GameObject}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class GameObjectSchemaSchema implements GameObjectSchema {

    public static final String OBJECTTYPE_FIELD = "objecttype";

    public static final String OBJECTID_FIELD = "objectid";

    @Override
    public void createSchema(Object db) {
        var odb = (ODatabaseDocument) db;
        var c = odb.createVertexClass("Type" + GameObject.OBJECT_TYPE);
        c.createProperty(OBJECTID_FIELD, OType.LONG);
        c.createProperty(OBJECTTYPE_FIELD, OType.STRING);
        try (var q = odb.command(
                "CREATE INDEX GameObject_type_id ON GameObject (objecttype, objectid) UNIQUE METADATA {ignoreNullValues: false}")) {
        }
    }

}
