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
package com.anrisoftware.dwarfhustle.model.db.orientdb.storages;

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameObjectSchemaSchema.OBJECTID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameObjectSchemaSchema.OBJECTTYPE_FIELD;
import static com.orientechnologies.orient.core.record.ODirection.IN;
import static com.orientechnologies.orient.core.record.ODirection.OUT;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.eclipse.collections.api.LongIterable;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

/**
 * Stores and retrieves the properties of a {@link StoredObject} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public abstract class AbstractGameObjectStorage implements GameObjectStorage {

    @Override
    public void store(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        v.setProperty(OBJECTID_FIELD, go.getId());
        v.setProperty(OBJECTTYPE_FIELD, go.getObjectType());
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        go.setRid(v.getIdentity());
        go.setId((Long) v.getProperty(OBJECTID_FIELD));
        return go;
    }

    protected void storePosIdsMap(OElement v, MapChunk mc, ObjectLongMap<? extends GameBlockPos> map, String idClass,
            String field, String idField) {
        Map<String, OElement> omap = Maps.mutable.ofInitialCapacity(map.size());
        map.forEachKeyValue((pos, id) -> {
            var o = new ODocument(idClass);
            o.setProperty(idField, id);
            omap.put(pos.toSaveString(), o);
        });
        v.setProperty(field, omap, OType.EMBEDDEDMAP);
    }

    protected OResultSet queryByObjectId(ODatabaseDocument odb, String objectType, long id) {
        return odb.query("SELECT * from ? where objecttype = ? and objectid = ?", objectType, objectType, id);
    }

    protected OResultSet queryByObjectIds(ODatabaseDocument odb, String objectType, LongIterable ids) {
        var s = new StringBuilder("SELECT * from ? where objecttype = ? and objectid in [");
        for (var i = ids.longIterator(); i.hasNext();) {
            s.append(i.next());
            s.append(",");
        }
        s.deleteCharAt(s.length() - 1);
        s.append("]");
        return odb.query(s.toString(), objectType, objectType);
    }

    protected long retrieveEdgeOutToId(OElement v, String edgeClass) {
        return retrieveEdgeId(OUT, IN, v, edgeClass);
    }

    protected long retrieveEdgeOutFromId(OElement v, String edgeClass) {
        return retrieveEdgeId(OUT, OUT, v, edgeClass);
    }

    protected long retrieveEdgeInToId(OElement v, String edgeClass) {
        return retrieveEdgeId(IN, IN, v, edgeClass);
    }

    protected long retrieveEdgeInFromId(OElement v, String edgeClass) {
        return retrieveEdgeId(IN, OUT, v, edgeClass);
    }

    private long retrieveEdgeId(ODirection edir, ODirection vdir, OElement v, String edgeClass) {
        var edges = v.asVertex().get().getEdges(edir, edgeClass);
        for (var e : edges) {
            return e.getVertex(vdir).getProperty(OBJECTID_FIELD);
        }
        return 0;
    }

    protected String toString(LocalDateTime time) {
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
