package com.anrisoftware.dwarfhustle.model.api.objects;

import lombok.RequiredArgsConstructor;

/**
 * {@link MapBlock} flags.
 */
@RequiredArgsConstructor
public enum MapBlockFlags {

    HIDDEN(0b00000000),

    VISIBLE(0b00000001),

    FILLED(0b00000010),

    EMPTY(0b00000100),

    LIQUID(0b00001000),

    RAMP(0b00010000),

    FLOOR(0b00100000),

    ROOF(0b01000000),

    DISCOVERED(0b10000000);

    public final int flag;
}
