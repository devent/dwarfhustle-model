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

    public final GameBlockPos ep;

    public final float centerx;

    public final float centery;

    public final float centerz;

    public final float extentx;

    public final float extenty;

    public final float extentz;

    public GameChunkPos(int mapid, int x, int y, int z, int ex, int ey, int ez) {
        this(new GameBlockPos(mapid, x, y, z), new GameBlockPos(mapid, ex, ey, ez));
    }

    public GameChunkPos(GameBlockPos pos, GameBlockPos endPos) {
        super(pos.getMapid(), pos.getX(), pos.getY(), pos.getZ());
        this.ep = endPos;
        this.extentx = (endPos.x - pos.x) / 2f;
        this.extenty = (endPos.y - pos.y) / 2f;
        this.extentz = (endPos.z - pos.z) / 2f;
        this.centerx = pos.x + extentx;
        this.centery = pos.y + extenty;
        this.centerz = pos.z + extentz;
    }

    /**
     * Returns string that can be used to store the block position. For example:
     * <ul>
     * <li>{@code 0/0/0/0/0/0/0}
     * <li>{@code 1/0/0/0/64/64/64}
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
        var pos = new GameBlockPos(toInt(split[0]), toInt(split[1]), toInt(split[2]), toInt(split[3]));
        var ep = new GameBlockPos(toInt(split[0]), toInt(split[4]), toInt(split[5]), toInt(split[6]));
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
