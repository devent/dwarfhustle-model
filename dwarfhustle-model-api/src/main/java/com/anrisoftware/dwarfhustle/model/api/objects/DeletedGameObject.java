package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
public class DeletedGameObject extends GameObject {

    public static final int OBJECT_TYPE = DeletedGameObject.class.getSimpleName().hashCode();

    private int type;

    public DeletedGameObject(int type, long id) {
        super(id);
        this.type = type;
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeInt(type);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.type = in.readInt();
    }
}
