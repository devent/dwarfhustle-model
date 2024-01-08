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
 * aFLOAT with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.db.orientdb.schemas;

import com.anrisoftware.dwarfhustle.model.api.objects.CenterExtent;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link CenterExtent}. This is only a partial
 * schema used together with {@link MapChunk} and {@link MapBlock}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class CenterExtentSchema {

    public static final String CENTER_X_FIELD = "ccx";

    public static final String CENTER_Y_FIELD = "ccy";

    public static final String CENTER_Z_FIELD = "ccz";

    public static final String EXTENT_X_FIELD = "cex";

    public static final String EXTENT_Y_FIELD = "cey";

    public static final String EXTENT_Z_FIELD = "cez";

    public void createSchema(Object db, OClass c) {
        c.createProperty(CENTER_X_FIELD, OType.FLOAT);
        c.createProperty(CENTER_Y_FIELD, OType.FLOAT);
        c.createProperty(CENTER_Z_FIELD, OType.FLOAT);
        c.createProperty(EXTENT_X_FIELD, OType.FLOAT);
        c.createProperty(EXTENT_Y_FIELD, OType.FLOAT);
        c.createProperty(EXTENT_Z_FIELD, OType.FLOAT);
	}

}
