package com.anrisoftware.dwarfhustle.model.api.objects;

/**
 * Stores and retrieves {@link GameObject} game objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface GameObjects<K, V extends GameObject> {

    public V get(K key);
}
