package com.anrisoftware.dwarfhustle.model.knowledge.evrete

import static org.junit.jupiter.params.provider.Arguments.of

import java.util.stream.Stream

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir

import groovy.util.logging.Slf4j

/**
 * @see TerrainCreateKnowledge
 */
@Slf4j
class TerrainCreateKnowledgeTest {

    static TerrainCreateKnowledge knowledge

    @BeforeAll
    static void setUp() {
        this.knowledge = new TerrainCreateKnowledge()
    }

    static Stream ruleset_declaration() {
        MapBlock block
        MapBlock[] neighbors
        def args = []

        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        neighbors[NeighboringDir.N.ordinal()] = new MapBlock(1, new GameBlockPos(10, 9, 0))
        args << of(0, block, neighbors, 3856880631809l, 2)

        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        neighbors[NeighboringDir.N.ordinal()] = new MapBlock(1, new GameBlockPos(10, 9, 0))
        args << of(837, block, neighbors, 3594887626753l, 1)

        block = new MapBlock(1, new GameBlockPos(10, 10, 0))
        neighbors = new MapBlock[NeighboringDir.values().length]
        neighbors[NeighboringDir.N.ordinal()] = new MapBlock(1, new GameBlockPos(10, 9, 0))
        args << of(898, block, neighbors, 3856880631809l, 2)

        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource
    void ruleset_declaration(long mid, MapBlock block, MapBlock[] neighbors, long eM, int eP) {
        def session = knowledge.createSession()
        session.insertAndFire(mid, block, neighbors)
        println block
        assert block.material == eM
        assert block.p.bits == eP
    }
}