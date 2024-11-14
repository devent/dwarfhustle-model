/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

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

    OBJECT_TREE_TRUNK_NAME("object-tree-trunk".hashCode());

    public final int hash;
}
