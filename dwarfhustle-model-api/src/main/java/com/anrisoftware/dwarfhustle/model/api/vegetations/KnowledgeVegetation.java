/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.eclipse.collections.api.set.primitive.IntSet;

import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObjectType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Grasses, shrubs, trees.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public abstract class KnowledgeVegetation extends KnowledgeObjectType {

    public static final int OBJECT_TYPE = KnowledgeVegetation.class.getSimpleName().hashCode();

    public static final String TYPE = "Vegetation";

    /**
     * Season(s) of the growing time.
     */
    public IntSet growingSeason;

    /**
     * Growing speed in units/day giving best conditions.
     */
    public float growingSpeed;

    /**
     * Minimum temperature for the plant, below the plant will die.
     */
    public float growingMinTemp;

    /**
     * Maximum temperature for the plant, above the plant will die.
     */
    public float growingMaxTemp;

    /**
     * Optimal temperature for the plant, above or below the plant will receive
     * growth penalties.
     */
    public float growingOptTemp;

    /**
     * Defines where the plant can grow.
     */
    public IntSet growingSoil;

    /**
     * Month(s) of the flowering period.
     */
    public IntSet floweringMonths;

    /**
     * Climate zone(s) where the plant grows.
     */
    public IntSet growingClimate;

    /**
     * The maximum size of roots in blocks.
     */
    public int rootMaxSize;

    /**
     * The maximum width of the vegetation in blocks.
     */
    public int widthMax;

    /**
     * The maximum height of the vegetation in blocks.
     */
    public int heightMax;

    /**
     * The maximum depth of the vegetation in blocks.
     */
    public int depthMax;

    public KnowledgeVegetation(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return KnowledgeVegetation.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeVegetation.TYPE;
    }

}
