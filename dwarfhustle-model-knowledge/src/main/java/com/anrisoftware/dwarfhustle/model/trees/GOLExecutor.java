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
package com.anrisoftware.dwarfhustle.model.trees;

import java.util.List;

import it.carmelolagamba.jcal.core.CellularAutomataExecutor;
import it.carmelolagamba.jcal.model.DefaultCell;
import it.carmelolagamba.jcal.model.DefaultStatus;

public class GOLExecutor extends CellularAutomataExecutor {

    @Override
    public DefaultCell singleRun(DefaultCell cell, List<DefaultCell> neighbors) {

        DefaultStatus dead = new DefaultStatus("dead", "0");
        DefaultStatus alive = new DefaultStatus("alive", "1");
        Long alives = neighbors.stream().filter(item -> item.currentStatus.equals(alive)).count();
        DefaultCell toReturn = new DefaultCell(null, cell.getRow(), cell.getCol());
        if (cell.currentStatus.equals(dead) && alives == 3) {
            toReturn.currentStatus = alive;
        } else if (cell.currentStatus.equals(alive) && (alives == 2 || alives == 3)) {
            toReturn.currentStatus = alive;
        } else {
            toReturn.currentStatus = dead;
        }
        return toReturn;
    }
}
