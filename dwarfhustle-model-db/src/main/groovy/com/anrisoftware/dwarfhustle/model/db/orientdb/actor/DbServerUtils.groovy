package com.anrisoftware.dwarfhustle.model.db.orientdb.actor

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

	def createServer() {
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
