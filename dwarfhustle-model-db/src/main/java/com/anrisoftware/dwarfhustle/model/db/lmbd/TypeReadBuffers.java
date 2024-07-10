package com.anrisoftware.dwarfhustle.model.db.lmbd;

import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapBuffer;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMapBuffer;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Grass;
import com.anrisoftware.dwarfhustle.model.api.vegetations.GrassBuffer;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Shrub;
import com.anrisoftware.dwarfhustle.model.api.vegetations.ShrubBuffer;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Tree;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeBranch;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeBranchBuffer;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeBuffer;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeLeaf;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeLeafBuffer;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeRoot;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeRootBuffer;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeTrunk;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeTrunkBuffer;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeTwig;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeTwigBuffer;

/**
 * Provides the map of object type to read buffer functions.
 */
public class TypeReadBuffers {

    public static final IntObjectMap<Function<DirectBuffer, GameObject>> TYPE_READ_BUFFERS;

    static {
        MutableIntObjectMap<Function<DirectBuffer, GameObject>> map = IntObjectMaps.mutable.empty();
        map.put(WorldMap.OBJECT_TYPE.hashCode(), (b) -> {
            return WorldMapBuffer.getWorldMap(b, 0, new WorldMap());
        });
        map.put(GameMap.OBJECT_TYPE.hashCode(), (b) -> {
            return GameMapBuffer.getGameMap(b, 0, new GameMap());
        });
        map.put(Grass.OBJECT_TYPE.hashCode(), (b) -> {
            return GrassBuffer.getGrass(b, 0, new Grass());
        });
        map.put(Shrub.OBJECT_TYPE.hashCode(), (b) -> {
            return ShrubBuffer.getShrub(b, 0, new Shrub());
        });
        map.put(Tree.OBJECT_TYPE.hashCode(), (b) -> {
            return TreeBuffer.getTree(b, 0, new Tree());
        });
        map.put(TreeBranch.OBJECT_TYPE.hashCode(), (b) -> {
            return TreeBranchBuffer.getTreeBranch(b, 0, new TreeBranch());
        });
        map.put(TreeLeaf.OBJECT_TYPE.hashCode(), (b) -> {
            return TreeLeafBuffer.getTreeLeaf(b, 0, new TreeLeaf());
        });
        map.put(TreeRoot.OBJECT_TYPE.hashCode(), (b) -> {
            return TreeRootBuffer.getTreeRoot(b, 0, new TreeRoot());
        });
        map.put(TreeTrunk.OBJECT_TYPE.hashCode(), (b) -> {
            return TreeTrunkBuffer.getTreeTrunk(b, 0, new TreeTrunk());
        });
        map.put(TreeTwig.OBJECT_TYPE.hashCode(), (b) -> {
            return TreeTwigBuffer.getTreeTwig(b, 0, new TreeTwig());
        });
        TYPE_READ_BUFFERS = map;
    }
}
