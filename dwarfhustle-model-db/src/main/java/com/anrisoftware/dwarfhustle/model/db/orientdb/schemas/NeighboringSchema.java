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

import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.D;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.DE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.DN;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.DNE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.DNW;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.DS;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.DSE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.DSW;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.DW;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.E;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.N;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.NE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.NW;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.S;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.SE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.SW;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.U;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.UE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.UN;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.UNE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.UNW;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.US;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.USE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.USW;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.UW;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.W;

import java.util.Map;

import org.eclipse.collections.api.factory.Maps;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link NeighboringDir}. This is only a partial
 * schema used together with {@link MapChunk} and {@link MapBlock}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class NeighboringSchema {

    public static final String NEIGHBOR_U_FIELD = "nu";

    public static final String NEIGHBOR_D_FIELD = "nd";

    public static final String NEIGHBOR_N_FIELD = "nn";

    public static final String NEIGHBOR_NE_FIELD = "nne";

    public static final String NEIGHBOR_E_FIELD = "ne";

    public static final String NEIGHBOR_SE_FIELD = "nse";

    public static final String NEIGHBOR_S_FIELD = "ns";

    public static final String NEIGHBOR_SW_FIELD = "nsw";

    public static final String NEIGHBOR_W_FIELD = "nw";

    public static final String NEIGHBOR_NW_FIELD = "nnw";

    public static final String NEIGHBOR_UN_FIELD = "nun";

    public static final String NEIGHBOR_UNE_FIELD = "nune";

    public static final String NEIGHBOR_UE_FIELD = "nue";

    public static final String NEIGHBOR_USE_FIELD = "nuse";

    public static final String NEIGHBOR_US_FIELD = "nus";

    public static final String NEIGHBOR_USW_FIELD = "nusw";

    public static final String NEIGHBOR_UW_FIELD = "nuw";

    public static final String NEIGHBOR_UNW_FIELD = "nunw";

    public static final String NEIGHBOR_DN_FIELD = "ndn";

    public static final String NEIGHBOR_DNE_FIELD = "ndne";

    public static final String NEIGHBOR_DE_FIELD = "nde";

    public static final String NEIGHBOR_DSE_FIELD = "ndse";

    public static final String NEIGHBOR_DS_FIELD = "nds";

    public static final String NEIGHBOR_DSW_FIELD = "ndsw";

    public static final String NEIGHBOR_DW_FIELD = "ndw";

    public static final String NEIGHBOR_DNW_FIELD = "ndnw";

    public static final Map<NeighboringDir, String> names;

    static {
        names = Maps.mutable.empty();
        names.put(U, NEIGHBOR_U_FIELD);
        names.put(D, NEIGHBOR_D_FIELD);
        names.put(N, NEIGHBOR_N_FIELD);
        names.put(NE, NEIGHBOR_NE_FIELD);
        names.put(E, NEIGHBOR_E_FIELD);
        names.put(SE, NEIGHBOR_SE_FIELD);
        names.put(S, NEIGHBOR_S_FIELD);
        names.put(SW, NEIGHBOR_SW_FIELD);
        names.put(W, NEIGHBOR_W_FIELD);
        names.put(NW, NEIGHBOR_NW_FIELD);
        names.put(UN, NEIGHBOR_UN_FIELD);
        names.put(UNE, NEIGHBOR_UNE_FIELD);
        names.put(UE, NEIGHBOR_UE_FIELD);
        names.put(USE, NEIGHBOR_USE_FIELD);
        names.put(US, NEIGHBOR_US_FIELD);
        names.put(USW, NEIGHBOR_USW_FIELD);
        names.put(UW, NEIGHBOR_UW_FIELD);
        names.put(UNW, NEIGHBOR_UNW_FIELD);
        names.put(DN, NEIGHBOR_DN_FIELD);
        names.put(DNE, NEIGHBOR_DNE_FIELD);
        names.put(DE, NEIGHBOR_DE_FIELD);
        names.put(DSE, NEIGHBOR_DSE_FIELD);
        names.put(DS, NEIGHBOR_DS_FIELD);
        names.put(DSW, NEIGHBOR_DSW_FIELD);
        names.put(DW, NEIGHBOR_DW_FIELD);
        names.put(DNW, NEIGHBOR_DNW_FIELD);
    }

    public void createSchema(Object db, OClass c) {
        c.createProperty(NEIGHBOR_U_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_D_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_N_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_NE_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_E_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_SE_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_S_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_SW_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_W_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_NW_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_UN_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_UNE_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_UE_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_USE_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_US_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_USW_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_UW_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_UNW_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_DN_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_DNE_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_DE_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_DSE_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_DS_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_DSW_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_DW_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_DNW_FIELD, OType.LONG);
    }

    public static String getName(NeighboringDir n) {
        return names.get(n);
    }

}
