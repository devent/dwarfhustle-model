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

import java.nio.ByteBuffer;

/**
 * Methods to read short, integer and long directly from a {@link ByteBuffer}.
 * No need to invoke {@link ByteBuffer#asShortBuffer()}, etc.
 */
public class BufferUtils {

    public static void writeShort(ByteBuffer b, short n) {
        b.put((byte) (n >> 8));
        b.put((byte) (n));
    }

    public static short readShort(ByteBuffer b) {
        return (short) ((b.get() & 255) << 8 | (b.get() & 255));
    }

    public static void writeInt(ByteBuffer b, int n) {
        b.put((byte) (n >> 24));
        b.put((byte) (n >> 16));
        b.put((byte) (n >> 8));
        b.put((byte) (n));
    }

    public static int readInt(ByteBuffer b) {
        return (b.get() & 255) << 24 | (b.get() & 255) << 16 | (b.get() & 255) << 8 | (b.get() & 255);
    }

    public static void writeLong(ByteBuffer b, long n) {
        b.put((byte) (n >> 56));
        b.put((byte) (n >> 48));
        b.put((byte) (n >> 40));
        b.put((byte) (n >> 32));
        b.put((byte) (n >> 24));
        b.put((byte) (n >> 16));
        b.put((byte) (n >> 8));
        b.put((byte) (n));
    }

    public static long readLong(ByteBuffer b) {
        return (b.get() & 255) << 56 | (b.get() & 255) << 48 | (b.get() & 255) << 40 | (b.get() & 255) << 32
                | (b.get() & 255) << 24 | (b.get() & 255) << 16 | (b.get() & 255) << 8 | (b.get() & 255);
    }

    public static double trunc(double value, int decimalpoint) {
        value = value * Math.pow(10, decimalpoint);
        value = Math.floor(value);
        value = value / Math.pow(10, decimalpoint);
        return value;
    }
}
