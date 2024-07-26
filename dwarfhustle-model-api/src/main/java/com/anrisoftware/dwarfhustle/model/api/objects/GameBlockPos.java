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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * X, Y and Z position of a {@link GameObject} on the game map.
 * <p>
 * Size 12 bytes
 * <ul>
 * <li>3*4(x,y,z)
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GameBlockPos implements Externalizable, StreamStorage {

    private static final long serialVersionUID = 1L;

    /**
     * Returns the index from the x/y/z position.
     */
    public static int calcIndex(int w, int h, int d, int sx, int sy, int sz, int x, int y, int z) {
        return (z - sz) * w * h + (y - sy) * w + x - sx;
    }

    /**
     * Returns the X position from the index.
     */
    public static int calcX(int i, int w, int sx) {
        return i % w + sx;
    }

    /**
     * Returns the Y position from the index.
     */
    public static int calcY(int i, int w, int sy) {
        return Math.floorMod(i / w, w) + sy;
    }

    /**
     * Returns the Z position from the index.
     */
    public static int calcZ(int i, int w, int h, int sz) {
        return (int) Math.floor(i / w / h) + sz;
    }

    /**
     * X position on the game map
     */
    public int x = -1;

    /**
     * Y position on the game map
     */
    public int y = -1;

    /**
     * Z position on the game map
     */
    public int z = -1;

    public int getDiffX(GameBlockPos pos) {
        return x - pos.x;
    }

    public int getDiffY(GameBlockPos pos) {
        return y - pos.y;
    }

    public int getDiffZ(GameBlockPos pos) {
        return z - pos.z;
    }

    /**
     * Returns string that can be used to store the block position: {@code X/Y/Z}
     */
    public String toSaveString() {
        return getX() + "/" + getY() + "/" + getZ();
    }

    /**
     * Returns the {@link GameBlockPos} parsed from the string.
     */
    public static GameBlockPos parse(String s) {
        var split = StringUtils.split(s, "/");
        var pos = new GameBlockPos(toInt(split[0]), toInt(split[1]), toInt(split[2]));
        return pos;
    }

    private static int toInt(String s) {
        return Integer.parseInt(s);
    }

    public GameBlockPos add(GameBlockPos p) {
        return new GameBlockPos(x + p.x, y + p.y, z + p.z);
    }

    public GameBlockPos addX(int n) {
        return new GameBlockPos(x + n, y, z);
    }

    public GameBlockPos addY(int n) {
        return new GameBlockPos(x, y + n, z);
    }

    public GameBlockPos addZ(int n) {
        return new GameBlockPos(x, y, z + n);
    }

    public GameBlockPos mul(int n) {
        return new GameBlockPos(x * n, y * n, z * n);
    }

    public boolean isEqual(int x2, int y2, int z2) {
        return x == x2 && y == y2 && z == z2;
    }

    public boolean isNegative() {
        return x < 0 || y < 0 || z < 0;
    }

    public boolean isOutBounds(int size) {
        return x >= size || y >= size || z >= size;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeStream(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readStream(in);
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        this.x = in.readInt();
        this.y = in.readInt();
        this.z = in.readInt();
    }

    public boolean equals(int x2, int y2, int z2) {
        return this.x == x2 && this.y == y2 && this.z == z2;
    }

    public void set(GameBlockPos pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

}
