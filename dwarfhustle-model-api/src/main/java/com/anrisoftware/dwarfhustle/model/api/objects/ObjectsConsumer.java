package com.anrisoftware.dwarfhustle.model.api.objects;

@FunctionalInterface
public interface ObjectsConsumer {

    void accept(int type, long id, int x, int y, int z);
}
