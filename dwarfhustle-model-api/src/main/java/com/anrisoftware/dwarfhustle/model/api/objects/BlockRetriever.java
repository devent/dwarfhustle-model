package com.anrisoftware.dwarfhustle.model.api.objects;

/**
 * Retrieves the {@link MapBlock}.
 * 
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface BlockRetriever {

    MapBlock getBlock(MapChunk chunk, GameBlockPos pos);

}
