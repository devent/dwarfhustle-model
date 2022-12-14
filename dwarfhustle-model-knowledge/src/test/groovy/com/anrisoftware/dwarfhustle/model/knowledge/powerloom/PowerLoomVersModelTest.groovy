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
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("materials.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("game-map.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("materials-sedimentary-stones.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("materials-igneous-intrusive-stones.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("materials-igneous-extrusive-stones.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("materials-metamorphic-stones.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("materials-metals.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("materials-metals-ores.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("materials-metals-alloys.plm"), UTF_8)), null);
        PLI.loadStream(InputStringStream.newInputStringStream(IOUtils.toString(PowerLoomVersModelTest.class.getResourceAsStream("example.plm"), UTF_8)), null);
        this.workingModule = "DWARFHUSTLE-EXAMPLE";
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "all (Stone ?type)",
        "all (Metal-Ore ?type)",
        "all (metal-ore-product ?x ?y Copper)",
        "all ?x (and (melting-point-material ?x ?t) (> ?t 2000))",
    ])
    void "test retrieve"(String retrieve) {
        printPowerLoomRetrieve(retrieve, workingModule, null);
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    void "test ask"(String ask) {
        //printPowerLoomAsk(ask, workingModule, null);
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
