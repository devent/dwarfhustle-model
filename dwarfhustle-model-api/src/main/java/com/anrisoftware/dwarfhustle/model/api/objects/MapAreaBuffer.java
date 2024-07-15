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
package com.anrisoftware.dwarfhustle.model.api.objects;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link MapArea} in a byte buffer.
 * 
 * 
 * <ul>
 * <li>@{code NWaa/NWoo} the {@link MapArea#nw};
 * <li>@{code SEaa/SEoo} the {@link MapArea#se};
 * </ul>
 * 
 * <pre>
 * long  0
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6    7
 *       NWaa NWaa NWoo NWoo SEaa SEaa SEoo SEoo
 * </pre>
 */
public class MapAreaBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = //
            2 * MapCoordinateBuffer.SIZE;

    private static final int NW_BYTES = 0 * 4;

    private static final int SE_BYTES = 2 * 4;

    public static void setNwLat(MutableDirectBuffer b, int off, float lat) {
        MapCoordinateBuffer.setLat(b, NW_BYTES + off, lat);
    }

    public static float getNwLat(DirectBuffer b, int off) {
        return MapCoordinateBuffer.getLat(b, NW_BYTES + off);
    }

    public static void setNwLon(MutableDirectBuffer b, int off, float lon) {
        MapCoordinateBuffer.setLon(b, NW_BYTES + off, lon);
    }

    public static float getNwLon(DirectBuffer b, int off) {
        return MapCoordinateBuffer.getLon(b, NW_BYTES + off);
    }

    public static void setSeLat(MutableDirectBuffer b, int off, float lat) {
        MapCoordinateBuffer.setLat(b, SE_BYTES + off, lat);
    }

    public static float getSeLat(DirectBuffer b, int off) {
        return MapCoordinateBuffer.getLat(b, SE_BYTES + off);
    }

    public static void setSeLon(MutableDirectBuffer b, int off, float lon) {
        MapCoordinateBuffer.setLon(b, SE_BYTES + off, lon);
    }

    public static float getSeLon(DirectBuffer b, int off) {
        return MapCoordinateBuffer.getLon(b, SE_BYTES + off);
    }

    public static void setNw(MutableDirectBuffer b, int off, MapCoordinate nw) {
        setNwLat(b, off, nw.lat);
        setNwLon(b, off, nw.lon);
    }

    public static MapCoordinate getNw(DirectBuffer b, int off, MapCoordinate nw) {
        nw.lat = getNwLat(b, off);
        nw.lon = getNwLon(b, off);
        return nw;
    }

    public static void setSe(MutableDirectBuffer b, int off, MapCoordinate se) {
        setSeLat(b, off, se.lat);
        setSeLon(b, off, se.lon);
    }

    public static MapCoordinate getSe(DirectBuffer b, int off, MapCoordinate se) {
        se.lat = getSeLat(b, off);
        se.lon = getSeLon(b, off);
        return se;
    }

    public static void setArea(MutableDirectBuffer b, int off, MapArea a) {
        setNw(b, off, a.nw);
        setSe(b, off, a.se);
    }

    public static MapArea getArea(DirectBuffer b, int off, MapArea a) {
        a.nw = getNw(b, off, a.nw);
        a.se = getSe(b, off, a.se);
        a.updateCenter();
        return a;
    }

}
