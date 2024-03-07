package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Objects that are stored in stream as compact as possible.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface StreamStorage {

    public void writeStream(DataOutput out) throws IOException;

    public void readStream(DataInput in) throws IOException;
}
