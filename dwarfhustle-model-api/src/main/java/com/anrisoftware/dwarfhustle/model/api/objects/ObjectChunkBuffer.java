package com.anrisoftware.dwarfhustle.model.api.objects;

import com.anrisoftware.dwarfhustle.model.api.map.ObjectType;

/**
 * Writes and reads {@link ObjectType} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} chunk CID;
 * <li>@{code P} parent chunk CID;
 * <li>@{code c} chunk size;
 * <li>@{code xyzXYZ} chunk position;
 * <li>@{code N} 26 CIDs of neighbors;
 * <li>@{code C} {@link CidGameChunkPosMapBuffer};
 * <li>@{code b} optionally {@link MapBlockBuffer};
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6    7    8    9         35        43
 *       iiii PPPP cccc xxxx yyyy zzzz XXXX YYYY ZZZZ NNNN x26. CCCC x8.. bbbb ....
 * </pre>
 */
public class ObjectChunkBuffer {

}
