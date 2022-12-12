package com.anrisoftware.dwarfhustle.model.knowledge.powerloom

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.stream.StreamSupport

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import edu.isi.powerloom.Environment;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator;
import edu.isi.powerloom.logic.TruthValue
import edu.isi.stella.InputStringStream;

/**
 * Tests the dwarf model dwarf-model.plm.
 *
 * @author Erwin Müller
 */
class PowerLoomVersModelTest {

    static String workingModule

    @BeforeAll
    public static void setupLoom() throws IOException {
        PLI.initialize();
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("game-map.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("game-map-sedimentary-stones.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("game-map-igneous-intrusive-stones.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("game-map-igneous-extrusive-stones.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("game-map-metamorphic-stones.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("game-map-example.plm"), UTF_8)), null);
        this.workingModule = "GAME-MAP-EXAMPLE";
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "all (Stone ?type)",
    ])
    void "test retrieve"(String retrieve) {
        printPowerLoomRetrieve(retrieve, workingModule, null);
    }

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
