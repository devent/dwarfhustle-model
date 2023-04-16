package com.anrisoftware.dwarfhustle.model.api.materials

import org.junit.jupiter.api.Test

/**
 * @see KnowledgeObject
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class KnowledgeObjectTest {

    @Test
    void "object_rid2Id"() {
        assert KnowledgeObject.rid2Id(0) == 1
        assert KnowledgeObject.rid2Id(500) == 2147483648001
        assert KnowledgeObject.rid2Id(501) == 2151778615297
    }
}
