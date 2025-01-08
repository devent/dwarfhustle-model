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
package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static java.lang.Math.pow;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.lmdbjava.CopyFlags.MDB_CP_COMPACT;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DbiFlags.MDB_INTEGERKEY;
import static org.lmdbjava.Env.create;
import static org.lmdbjava.EnvFlags.MDB_WRITEMAP;

import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lmdbjava.ByteArrayProxy;
import org.lmdbjava.ByteBufferProxy;
import org.lmdbjava.DirectBufferProxy;
import org.lmdbjava.Verifier;

/**
 * Test {@link ByteBufferProxy}.
 */
@Disabled
public class ByteBufferProxyTest {

	@Test
	void test_buffer_array(@TempDir Path tmp) {
		final long mapSize = (long) (100 * pow(10, 6));
		final var env = create(ByteArrayProxy.PROXY_BA).setMapSize(mapSize).setMaxDbs(2).open(tmp.toFile(),
				MDB_WRITEMAP);
		final var db = env.openDbi("chunks", MDB_CREATE, MDB_INTEGERKEY);
		System.out.println(tmp);
		db.close();
		System.out.println(env.info());
		System.out.println(env.stat());
		env.close();
	}

	@Test
	void test_buffer_optional(@TempDir Path tmp, @TempDir Path copy) {
		final long mapSize = (long) (100 * pow(10, 6));
		final var env = create(ByteBufferProxy.PROXY_OPTIMAL).setMapSize(mapSize).setMaxDbs(Verifier.DBI_COUNT)
                .open(tmp.toFile());
		final var db = env.openDbi("chunks", MDB_CREATE, MDB_INTEGERKEY);
		System.out.println(tmp);
		final Verifier v = new Verifier(env);
        final long r = v.runFor(20, SECONDS);
		System.out.println("Records verified: " + r);
		db.close();
		env.copy(copy.toFile(), MDB_CP_COMPACT);
		env.close();
		System.out.println(copy);
	}

	@Test
	void test_buffer_direct(@TempDir Path tmp, @TempDir Path copy) {
		final long mapSize = (long) (100 * pow(10, 6));
		final var env = create(DirectBufferProxy.PROXY_DB).setMapSize(mapSize).setMaxDbs(2).open(tmp.toFile(),
				MDB_WRITEMAP);
		final var db = env.openDbi("chunks", MDB_CREATE, MDB_INTEGERKEY);
		System.out.println(tmp);
		db.close();
		System.out.println(env.info());
		System.out.println(env.stat());
		env.copy(copy.toFile(), MDB_CP_COMPACT);
		env.close();
		System.out.println(copy);
	}

}
