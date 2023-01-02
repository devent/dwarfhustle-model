/*
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 * Released as open-source under the Apache License, Version 2.0.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model
 * ****************************************************************************
 *
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model is a derivative work based on Josua Tippetts' C++ library:
 * http://accidentalnoise.sourceforge.net/index.html
 * ****************************************************************************
 *
 * Copyright (C) 2011 Joshua Tippetts
 *
 *   This software is provided 'as-is', without any express or implied
 *   warranty.  In no event will the authors be held liable for any damages
 *   arising from the use of this software.
 *
 *   Permission is granted to anyone to use this software for any purpose,
 *   including commercial applications, and to alter it and redistribute it
 *   freely, subject to the following restrictions:
 *
 *   1. The origin of this software must not be misrepresented; you must not
 *      claim that you wrote the original software. If you use this software
 *      in a product, an acknowledgment in the product documentation would be
 *      appreciated but is not required.
 *   2. Altered source versions must be plainly marked as such, and must not be
 *      misrepresented as being the original software.
 *   3. This notice may not be removed or altered from any source distribution.
 *
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model bundles and uses the RandomCL library:
 * https://github.com/bstatcomp/RandomCL
 * ****************************************************************************
 *
 * BSD 3-Clause License
 *
 * Copyright (c) 2018, Tadej Ciglarič, Erik Štrumbelj, Rok Češnovar. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.anrisoftware.dwarfhustle.model.generate;

import static com.anrisoftware.dwarfhustle.model.api.GameObject.toId;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.OBJECTID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.OBJECTTYPE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.Z_FIELD;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.anrisoftware.dwarfhustle.model.api.Path;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class Worker {

	/**
	 * Factory to create {@link Worker}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface WorkerFactory {

		Worker create(OrientDB db);
	}

	private ExecutorService pool;

	@Inject
	@Assisted
	private OrientDB db;

	@Inject
	private IDGenerator generator;

	public Worker() {
		this.pool = Executors.newFixedThreadPool(8);
	}

	public void generateMap(GenerateMapMessage m) {
		generateNodes(m);
		generatePaths(m);
		pool.shutdown();
	}

	private List<List<List<OVertex>>> nodes;

	@SneakyThrows
	private void generateNodes(GenerateMapMessage m) {
		log.debug("generateNodes");
		MutableList<List<List<OVertex>>> nodesidsz = Lists.mutable.ofInitialCapacity(m.depth);
		MutableList<CompletableFuture<Void>> tasks = Lists.mutable.ofInitialCapacity(m.getSize());
		for (int z = 0; z < m.depth; z++) {
			MutableList<List<OVertex>> nodesidsy = Lists.mutable.ofInitialCapacity(m.height);
			nodesidsz.add(nodesidsy);
			for (int y = 0; y < m.height; y++) {
				MutableList<OVertex> nodesidsx = Lists.mutable.ofInitialCapacity(m.width);
				nodesidsy.add(nodesidsx);
				final int zz = z;
				final int yy = y;
				addTask(m, tasks, db -> {
					generateNodes(m, db, nodesidsx, zz, yy);
				});
			}
		}
		int done = 0;
		for (CompletableFuture<Void> task : tasks) {
			task.join();
			done++;
			log.trace("Task still running {}", tasks.size() - done);
		}
		this.nodes = nodesidsz.asUnmodifiable();
	}

	@SneakyThrows
	private void generateNodes(GenerateMapMessage m, ODatabaseSession db, MutableList<OVertex> nodesidsx, int z,
			int y) {
		// log.trace("generateNodes {}/{}", z, y);
		var ids = generator.batch(m.width);
		db.declareIntent(new OIntentMassiveInsert());
		for (int x = 0; x < m.width; x++) {
			var v = db.newVertex(MapTile.TYPE);
			long id = toId(ids.pop());
			v.setProperty(OBJECTID_FIELD, id);
			v.setProperty(OBJECTTYPE_FIELD, MapTile.TYPE);
			v.setProperty(MAPID_FIELD, m.mapid);
			v.setProperty(X_FIELD, x);
			v.setProperty(Y_FIELD, y);
			v.setProperty(Z_FIELD, z);
			v.save();
			nodesidsx.add(v);
		}
		db.declareIntent(null);
	}

	@SneakyThrows
	private void generatePaths(GenerateMapMessage m) {
		log.debug("generatePaths");
		MutableList<CompletableFuture<Void>> tasks = Lists.mutable.ofInitialCapacity(m.getSize());
		try (var db = this.db.open(m.database, m.user, m.password)) {
			pathsMiddleAllDirections(m, db, tasks);
			pathsMiddleLeftRight(m, db, tasks);
			pathsMiddleTopBottom(m, db, tasks);
			pathsTopLeftRight(m, db, tasks);
			pathsBottomLeftRight(m, db, tasks);
			pathsTopTopBottom(m, db, tasks);
			pathsBottomTopBottom(m, db, tasks);
			pathsEdges(m, db, tasks);
		}
		int done = 0;
		for (CompletableFuture<Void> task : tasks) {
			task.join();
			done++;
			log.trace("Task still running {}", tasks.size() - done);
		}
		System.out.println("done"); // TODO
	}

	private void saveEdges(List<OEdge> edges, GenerateMapMessage m, MutableList<CompletableFuture<Void>> tasks) {
		for (OEdge edge : edges) {
			edge.save();
		}
	}

	private void addTask(GenerateMapMessage m, MutableList<CompletableFuture<Void>> tasks,
			Consumer<ODatabaseSession> run) {
		tasks.add(CompletableFuture.runAsync(() -> {
			try (var db = this.db.open(m.database, m.user, m.password)) {
				run.accept(db);
			}
		}, pool));
	}

	private void pathsEdges(GenerateMapMessage m, ODatabaseDocument db, MutableList<CompletableFuture<Void>> tasks) {
		List<OEdge> e = Lists.mutable.withInitialCapacity(7);
		// bottom south west edge
		int x = 0;
		int y = 0;
		int z = 0;
		OVertex n = nodes.get(x).get(y).get(z);
		e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
		// top south west edge
		x = 0;
		y = 0;
		z = m.depth - 1;
		n = nodes.get(x).get(y).get(z);
		e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
		// bottom south east edge
		x = m.width - 1;
		y = 0;
		z = 0;
		n = nodes.get(x).get(y).get(z);
		e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
		// top south east edge
		x = m.width - 1;
		y = 0;
		z = m.depth - 1;
		n = nodes.get(x).get(y).get(z);
		e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
		// bottom north east edge
		x = m.width - 1;
		y = m.height - 1;
		z = 0;
		n = nodes.get(x).get(y).get(z);
		e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
		// top north east edge
		x = m.width - 1;
		y = m.height - 1;
		z = m.depth - 1;
		n = nodes.get(x).get(y).get(z);
		e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
		// bottom north west edge
		x = 0;
		y = m.height - 1;
		z = 0;
		n = nodes.get(x).get(y).get(z);
		e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
		// top north west edge
		x = 0;
		y = m.height - 1;
		z = m.depth - 1;
		n = nodes.get(x).get(y).get(z);
		e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
		e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
		// save
		saveEdges(e, m, tasks);
	}

	/**
	 * Paths bottom z=0 on the bottom x=0 and top x=width-1.
	 */
	private void pathsBottomTopBottom(GenerateMapMessage m, ODatabaseDocument db,
			MutableList<CompletableFuture<Void>> tasks) {
		int z = 0;
		int x = 0;
		for (int y = 1; y < m.width - 1; y++) {
			OVertex n = nodes.get(z).get(y).get(y);
			List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 1);
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
			// up
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
			// save
			saveEdges(e, m, tasks);
		}
		x = m.width - 1;
		for (int y = 1; y < m.width - 1; y++) {
			OVertex n = nodes.get(z).get(y).get(y);
			List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 1);
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
			// up
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
			// save
			saveEdges(e, m, tasks);
		}
	}

	/**
	 * Paths top z=depth-1 on the bottom x=0 and top x=width-1.
	 */
	private void pathsTopTopBottom(GenerateMapMessage m, ODatabaseDocument db,
			MutableList<CompletableFuture<Void>> tasks) {
		int z = m.depth - 1;
		int x = 0;
		for (int y = 1; y < m.width - 1; y++) {
			OVertex n = nodes.get(z).get(y).get(y);
			List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 1);
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
			// down
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
			// save
			saveEdges(e, m, tasks);
		}
		x = m.width - 1;
		for (int y = 1; y < m.width - 1; y++) {
			OVertex n = nodes.get(z).get(y).get(y);
			List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 1);
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
			// down
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
			// save
			saveEdges(e, m, tasks);
		}
	}

	/**
	 * Paths bottom z=0 on the left y=0 and right y=height-1.
	 */
	private void pathsBottomLeftRight(GenerateMapMessage m, ODatabaseDocument db,
			MutableList<CompletableFuture<Void>> tasks) {
		int z = 0;
		int y = 0;
		for (int x = 1; x < m.width - 1; x++) {
			OVertex n = nodes.get(z).get(y).get(x);
			List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 1);
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
			// up
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
			// save
			saveEdges(e, m, tasks);
		}
		y = m.height - 1;
		for (int x = 1; x < m.width - 1; x++) {
			OVertex n = nodes.get(z).get(y).get(x);
			List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 1);
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
			// up
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
			// save
			saveEdges(e, m, tasks);
		}
	}

	/**
	 * Paths top z=depth-1 on the left y=0 and right y=height-1.
	 */
	private void pathsTopLeftRight(GenerateMapMessage m, ODatabaseDocument db,
			MutableList<CompletableFuture<Void>> tasks) {
		int z = m.depth - 1;
		int y = 0;
		for (int x = 1; x < m.width - 1; x++) {
			OVertex n = nodes.get(z).get(y).get(x);
			List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 1);
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
			// down
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
			// save
			saveEdges(e, m, tasks);
		}
		y = m.height - 1;
		for (int x = 1; x < m.width - 1; x++) {
			OVertex n = nodes.get(z).get(y).get(x);
			List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 1);
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
			// down
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
			e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
			// save
			saveEdges(e, m, tasks);
		}
	}

	/**
	 * Paths middle 0<z<depth-1 on the top x=0 and bottom x=width-1.
	 */
	private void pathsMiddleTopBottom(GenerateMapMessage m, ODatabaseDocument db,
			MutableList<CompletableFuture<Void>> tasks) {
		for (int z = 1; z < m.depth - 1; z++) {
			int x = 0;
			for (int y = 1; y < m.height - 1; y++) {
				OVertex n = nodes.get(z).get(y).get(y);
				List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 5 + 2);
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
				// up
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
				// down
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
				// save
				saveEdges(e, m, tasks);
			}
			x = m.width - 1;
			for (int y = 1; y < m.height - 1; y++) {
				OVertex n = nodes.get(z).get(y).get(y);
				List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 5 + 2);
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
				// up
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
				// down
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
				// save
				saveEdges(e, m, tasks);
			}
		}
	}

	/**
	 * Paths middle 0<z<depth-1 on the left y=0 and right y=height-1.
	 */
	private void pathsMiddleLeftRight(GenerateMapMessage m, ODatabaseDocument db,
			MutableList<CompletableFuture<Void>> tasks) {
		for (int z = 1; z < m.depth - 1; z++) {
			int y = 0;
			for (int x = 1; x < m.width - 1; x++) {
				OVertex n = nodes.get(z).get(y).get(x);
				List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 5 + 2);
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
				// up
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
				// down
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
				// save
				saveEdges(e, m, tasks);
			}
			y = m.height - 1;
			for (int x = 1; x < m.width - 1; x++) {
				OVertex n = nodes.get(z).get(y).get(x);
				List<OEdge> e = Lists.mutable.withInitialCapacity(5 + 5 + 5 + 2);
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
				// up
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
				// down
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
				// save
				saveEdges(e, m, tasks);
			}
		}
	}

	/**
	 * Paths for tiles that have all directions available 0<z<depth-1, 0<y<height-1,
	 * 0<x<width-1.
	 * <p>
	 * edges = 26*(depth-2)*(height-2)*(width-2)
	 */
	private void pathsMiddleAllDirections(GenerateMapMessage m, ODatabaseDocument db,
			MutableList<CompletableFuture<Void>> tasks) {
		for (int z = 1; z < m.depth - 1; z++) {
			for (int y = 1; y < m.height - 1; y++) {
				for (int x = 1; x < m.width - 1; x++) {
					OVertex n = nodes.get(z).get(y).get(x);
					List<OEdge> e = Lists.mutable.withInitialCapacity(8 + 8 + 8 + 2);
					e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
					// up
					e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x), Path.UPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
					// down
					e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x), Path.DPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
					// save
					saveEdges(e, m, tasks);
				}
			}
		}
	}

}
