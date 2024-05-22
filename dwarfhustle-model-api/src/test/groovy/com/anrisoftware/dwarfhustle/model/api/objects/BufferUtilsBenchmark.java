package com.anrisoftware.dwarfhustle.model.api.objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

/**
 * Benchmark test of buffers.
 */
class BufferUtilsBenchmark {

    static int chunk = 1024 * 10;

    static byte[] data;

    static ByteBuffer buffer;

    @BeforeAll
    static void setupRules() {
        data = new byte[chunk];
        buffer = ByteBuffer.wrap(data);
        var ib = buffer.asIntBuffer();
        for (int i = 0; i < buffer.limit(); i += 4) {
            ib.put(9999);
        }
    }

    @RepeatedTest(10000)
    void benchmark_to_intBuffer() {
        for (int i = 0; i < buffer.limit() / chunk; i += chunk) {
            buffer.position(i);
            var ib = buffer.asIntBuffer();
            for (int n = 0; n < chunk; n += 4) {
                int d = ib.get();
                assertThat(d, is(9999));
            }
        }
    }

    @RepeatedTest(10000)
    void benchmark_read_int() {
        int size = 4;
        var b = buffer;
        for (int i = 0; i < buffer.limit() / chunk; i += chunk) {
            b.position(i);
            for (int n = 0; n < chunk; n += size) {
                int d = BufferUtils.readInt(b);
                assertThat(d, is(9999));
            }
        }
    }

}
