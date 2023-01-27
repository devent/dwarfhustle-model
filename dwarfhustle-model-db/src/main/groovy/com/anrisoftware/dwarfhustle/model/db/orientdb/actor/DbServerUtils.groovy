/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor

import com.orientechnologies.orient.core.Orient
import com.orientechnologies.orient.server.OServer
import com.orientechnologies.orient.server.OServerMain

import groovy.util.logging.Slf4j

/**
 * Utilities to create embedded database server for testing.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class DbServerUtils {

	OServer server

	def createServer(def rootPath = null) {
		if (rootPath) {
			System.setProperty(Orient.ORIENTDB_HOME, rootPath);
		}
		server = OServerMain.create();
		def config = DbServerUtils.class.getResourceAsStream("/orientdb-test-config.xml")
		assert config != null
		server.startup(config)
		server.activate();
	}

	def shutdownServer() {
		server.shutdown()
	}
}
