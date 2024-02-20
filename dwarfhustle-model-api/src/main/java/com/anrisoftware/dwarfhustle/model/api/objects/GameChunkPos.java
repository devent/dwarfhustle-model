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

import org.apache.commons.lang3.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Start position and end position and a {@link MapChunk}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class GameChunkPos extends GameBlockPos {

    private static final long serialVersionUID = 1L;

    /**
     * Builds {@link GameChunkPos}.
     * 
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public static class GameChunkPosBuilder {

        private int sx, sy, sz, ex, ey, ez;

        public GameChunkPosBuilder sx(int sx) {
            this.sx = sx;
            return this;
        }

        public GameChunkPosBuilder sy(int sy) {
            this.sy = sy;
            return this;
        }

        public GameChunkPosBuilder sz(int sz) {
            this.sz = sz;
            return this;
        }

        public GameChunkPosBuilder ex(int ex) {
            this.ex = ex;
            return this;
        }

        public GameChunkPosBuilder ey(int ey) {
            this.ey = ey;
            return this;
        }

        public GameChunkPosBuilder ez(int ez) {
            this.ez = ez;
            return this;
        }

        public GameChunkPos build() {
            return new GameChunkPos(new GameBlockPos(sx, sy, sz), new GameBlockPos(ex, ey, ez));
        }
    }

    public static GameChunkPosBuilder builder() {
        return new GameChunkPosBuilder();
    }

    public final GameBlockPos ep;

    public GameChunkPos(int sx, int sy, int sz, int ex, int ey, int ez) {
        this(new GameBlockPos(sx, sy, sz), new GameBlockPos(ex, ey, ez));
    }

    public GameChunkPos(GameBlockPos pos, GameBlockPos endPos) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.ep = endPos;
    }

    /**
     * Returns string that can be used to store the block position:
     * {@code SX/SY/SZ/EX/EY/EZ} For example:
     * <ul>
     * <li>{@code 0/0/0/0/0/0}
     * <li>{@code 0/0/0/64/64/64}
     * </ul>
     */
    @Override
    public String toSaveString() {
        return super.toSaveString() + "/" + ep.getX() + "/" + ep.getY() + "/" + ep.getZ();
    }

    /**
     * Returns the {@link GameChunkPos} parsed from the string.
     */
    public static GameChunkPos parse(String s) {
        var split = StringUtils.split(s, "/");
        var pos = new GameBlockPos(toInt(split[0]), toInt(split[1]), toInt(split[2]));
        var ep = new GameBlockPos(toInt(split[3]), toInt(split[4]), toInt(split[5]));
        return new GameChunkPos(pos, ep);
    }

    private static int toInt(String s) {
        return Integer.parseInt(s);
    }

    public int getSizeX() {
        return ep.x - x;
    }

    public int getSizeY() {
        return ep.y - y;
    }

    public int getSizeZ() {
        return ep.z - z;
    }
}
