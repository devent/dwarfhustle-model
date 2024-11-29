package com.anrisoftware.dwarfhustle.model.db.cache

import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * @see MapObject
 * 
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapObjectTest {

    @ParameterizedTest
    @CsvSource([
        "2222222222,123,123",
    ])
    @Timeout(20l)
    void "calc id hash"(long map, int index, int expected) {
        assert MapObject.calcId(map, index) == new MapObject(map, index, 1).hashCode()
    }
}
