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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.function.Consumer;

import lombok.SneakyThrows;

/**
 * Stores {@link MapBlock} as an indexed byte array.
 * <p>
 * Size 1 bytes empty.
 * <ul>
 * <li>1(empty)
 * <li>chunkSize*chunkSize*chunkSize*BLOCK_SIZE_BYTES
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlocksStore implements Serializable, Externalizable, StreamStorage {

    private static final long serialVersionUID = 1L;

    public static final int BLOCK_SIZE_BYTES = 376;

    public static int calcIndex(int w, int h, int x, int y, int z) {
        return z * w * h + y * w + x;
    }

    private byte[] buffer;

    private int chunkSize;

    private int size;

    public MapBlocksStore(int chunkSize) {
        this.chunkSize = chunkSize;
        this.size = chunkSize * chunkSize * chunkSize;
        this.buffer = new byte[BLOCK_SIZE_BYTES * size];
    }

    public void setBlocks(Iterable<MapBlock> blocks) {
        for (var block : blocks) {
            setBlock(block);
        }
    }

    @SneakyThrows
    public synchronized void setBlock(MapBlock block) {
        int index = calcIndex(chunkSize, chunkSize, block.pos.x, block.pos.y, block.pos.z) % size;
        int pos = index * BLOCK_SIZE_BYTES;
        var stream = new ByteArrayOutputStream(BLOCK_SIZE_BYTES);
        var dstream = new DataOutputStream(stream);
        block.writeStream(dstream);
        int size = stream.size();
        dstream.close();
        System.arraycopy(stream.toByteArray(), 0, buffer, pos, size);
    }

    @SneakyThrows
    public synchronized MapBlock getBlock(GameBlockPos pos) {
        int index = calcIndex(chunkSize, chunkSize, pos.x, pos.y, pos.z) % size;
        int skip = index * BLOCK_SIZE_BYTES;
        var stream = new ByteArrayInputStream(buffer);
        stream.skip(skip);
        var dstream = new DataInputStream(stream);
        var block = new MapBlock();
        block.readStream(dstream);
        return block;
    }

    /**
     * Iterates over all {@link MapBlock}s in the storage. The order is not
     * necessarily the same as the order the blocks were set.
     */
    @SneakyThrows
    public synchronized void forEachValue(Consumer<MapBlock> consumer) {
        for (int i = 0; i < size; i++) {
            var stream = new ByteArrayInputStream(buffer);
            stream.skip(i * BLOCK_SIZE_BYTES);
            var ostream = new DataInputStream(stream);
            var block = new MapBlock();
            block.readStream(ostream);
            consumer.accept(block);
        }
    }

    public synchronized void setData(byte[] data) {
        this.buffer = data;
    }

    public synchronized byte[] getData() {
        return buffer;
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
        out.write(buffer);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        in.readFully(buffer);
    }

}
