package com.anrisoftware.dwarfhustle.model.api.objects;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Position of the map cursor.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class MapCursor {

    public final int z;

    public final int y;

    public final int x;

    public boolean equals(int z, int y, int x) {
        return this.z == z && this.y == y && this.x == x;
    }
}
