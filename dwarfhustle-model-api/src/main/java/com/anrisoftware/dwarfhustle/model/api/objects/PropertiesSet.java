package com.anrisoftware.dwarfhustle.model.api.objects;

/**
 * Simple bit set that for properties.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class PropertiesSet {

    private static int empty = 0x00000000;

    public int bits = empty;

    public PropertiesSet() {
        this(empty);
    }

    public PropertiesSet(int bits) {
        this.bits = bits;
    }

    public PropertiesSet replace(int replacebits) {
        this.bits = replacebits;
        return this;
    }

    public PropertiesSet set(int pos) {
        this.bits = (bits | (1 << pos));
        return this;
    }

    public PropertiesSet set(boolean set, int pos) {
        if (set) {
            return set(pos);
        } else {
            return clear(pos);
        }
    }

    public PropertiesSet sets(int setbits) {
        this.bits = bits | setbits;
        return this;
    }

    public PropertiesSet clear(int pos) {
        this.bits = (bits & (~(1 << pos)));
        return this;
    }

    public PropertiesSet clears(int clearbits) {
        this.bits = bits & ~clearbits;
        return this;
    }

    public PropertiesSet toggle(int pos) {
        this.bits = (bits ^ (1 << pos));
        return this;
    }

    public boolean contains(int otherbits) {
        return (bits & otherbits) == otherbits;
    }

    public boolean get(int pos) {
        return (bits & (1 << (pos))) != 0;
    }

    public boolean same(int otherbits) {
        return bits == otherbits;
    }

    @Override
    public String toString() {
        return Integer.toBinaryString(bits);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof PropertiesSet other) {
            return bits == other.bits;
        } else if (o instanceof Number other) {
            return other.equals(bits);
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = (result * PRIME) + bits;
        return result;
    }

}
