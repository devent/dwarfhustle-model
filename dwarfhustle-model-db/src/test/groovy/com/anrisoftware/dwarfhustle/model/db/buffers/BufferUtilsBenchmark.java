/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
