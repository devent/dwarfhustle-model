package com.anrisoftware.dwarfhustle.model.knowledge.powerloom

import java.util.stream.StreamSupport

import edu.isi.powerloom.Environment
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator
import edu.isi.powerloom.logic.TruthValue

/**
 * Utils to query PowerLoom.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class PowerLoomUtils {

	static void printPowerLoomRetrieve(String query, String module, Environment env) {
		PlIterator answer = PLI.sRetrieve(query, module, env);
		printSeparator();
		println "Answers to query `$query`"
		StreamSupport.stream(new PowerLoomSpliteratorSupport(answer.listify()).spliterator(), false).forEach({ v -> println v })
		printSeparator();
	}

	static void printPowerLoomAsk(String query, String module, Environment env) {
		TruthValue answer = PLI.sAsk(query, module, env);
		printSeparator();
		println "Answers to query `$query`"
		println answer
		printSeparator();
	}

	static void printSeparator() {
		println "-----------------------------------------"
	}
}
