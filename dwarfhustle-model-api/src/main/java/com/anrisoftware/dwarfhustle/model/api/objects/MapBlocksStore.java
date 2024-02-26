package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.Data;
import lombok.SneakyThrows;

/**
 * Stores {@link MapBlock} as an indexed byte array.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlocksStore implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int BLOCK_SIZE_BYTES = 2048;

    @Data
    private static class MapBlockItem implements Serializable {

        private static final long serialVersionUID = 1L;

        final int index;

        final int size;

        final MapBlock block;
    }

    public static int calcIndex(int w, int h, int x, int y, int z) {
        return z * w * h + y * w + x;
    }

    public static final byte[] int2Bytes(byte[] buff, int value) {
        buff[0] = (byte) (value >>> 24);
        buff[1] = (byte) (value >>> 16);
        buff[2] = (byte) (value >>> 8);
        buff[3] = (byte) (value);
        return buff;
    }

    public static final int bytes2Int(byte[] buff) {
        return buff[0] << 24 | buff[1] << 16 | buff[2] << 8 | buff[3];
    }

    private static byte[] sizebuffset = new byte[4];

    private static byte[] sizebuffget = new byte[4];

    private byte[] buffer;

    public MapBlocksStore(int chunkSize) {
        this.buffer = new byte[BLOCK_SIZE_BYTES * chunkSize * chunkSize * chunkSize];
    }

    @SneakyThrows
    public synchronized void setBlock(int w, int h, MapBlock block) {
        int index = calcIndex(w, h, block.pos.x, block.pos.y, block.pos.z) * BLOCK_SIZE_BYTES;
        var stream = new ByteArrayOutputStream(2048);
        new ObjectOutputStream(stream).writeObject(block);
        int size = stream.size();
        int2Bytes(sizebuffset, size);
        System.arraycopy(sizebuffset, 0, buffer, index, 4);
        System.arraycopy(stream.toByteArray(), 0, buffer, index + 4, size);
        System.out.println(buffer.length); // TODO
    }

    @SneakyThrows
    public synchronized MapBlock getBlock(int w, int h, GameBlockPos pos) {
        int index = calcIndex(w, h, pos.x, pos.y, pos.z) * BLOCK_SIZE_BYTES;
        var stream = new ByteArrayInputStream(buffer);
        stream.read(sizebuffget, index, 4);
        int size = bytes2Int(sizebuffget);
        var ostream = new ObjectInputStream(stream);
        var block = (MapBlock) ostream.readObject();
        return block;
    }

}
