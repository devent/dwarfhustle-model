package com.anrisoftware.dwarfhustle.model.trees;

import lombok.RequiredArgsConstructor;

/**
 * 
 */
@RequiredArgsConstructor
public enum VegetationKnowledgeObjects {

    OBJECT_TREE_ROOT_NAME("object-tree-root".hashCode()),

    OBJECT_TREE_LEAF_NAME("object-tree-leaf".hashCode()),

    OBJECT_TREE_TWIG_NAME("object-tree-twig".hashCode()),

    OBJECT_TREE_BRANCH_NAME("object-tree-branch".hashCode()),

    OBJECT_TREE_TRUNK_NAME("object-tree-trunk".hashCode()),

    MATERIAL_WOODS_NAME("materials-wood".hashCode());

    public final int hash;
}
