package com.anrisoftware.dwarfhustle.model.api.objects;

/**
 * Writes and reads {@link GameMap} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the ID;
 * <li>@{code w} parent chunk CID;
 * <li>@{code h} material KID;
 * <li>@{code S} object KID;
 * <li>@{code C} temperature;
 * <li>@{code c} light lux;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       iiii iiii iiii iiii
 * </pre>
 */
public class GameMapBuffer {

}
