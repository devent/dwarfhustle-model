/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.buffers;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.miscobjects.WoodLog;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link WoodLog} in a byte buffer.
 * <p>
 * See properties from {@link GameMapMaterialObjectBuffer}.
 * <ul>
 * <li>
 * </ul>
 *
 * <pre>
 * long  0                   1                   2                   3                   4                   5                   6
 * int   0         1         2         3         4         5         6         7         8         9         10        11        12
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25
 *       iiii iiii iiii iiii kkkk kkkk oooo oooo mmmm mmmm mmmm mmmm xxxx yyyy zzzz tttt wwww hhhh dddd llll pppp pppp MMMM MMMM MMMM MMMM
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class WoodLogBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameMapMaterialObjectBuffer.SIZE;

    public static void setWoodLog(MutableDirectBuffer b, int off, WoodLog o) {
        GameMapMaterialObjectBuffer.writeMaterialObject(b, off, o);
    }

    public static WoodLog getWoodLog(DirectBuffer b, int off, WoodLog o) {
        GameMapMaterialObjectBuffer.readMaterialObject(b, off, o);
        return o;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return getWoodLog(b, 0, new WoodLog());
    }

    @Override
    public int getObjectType() {
        return WoodLog.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return SIZE;
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        setWoodLog(b, 0, (WoodLog) go);
    }
}
