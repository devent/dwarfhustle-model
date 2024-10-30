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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.carmelolagamba.jcal.configuration.CellularAutomataConfiguration;
import it.carmelolagamba.jcal.configuration.CellularAutomataConfiguration.CellularAutomataConfigurationBuilder;
import it.carmelolagamba.jcal.core.CellularAutomata;
import it.carmelolagamba.jcal.model.DefaultCell;
import it.carmelolagamba.jcal.model.DefaultStatus;
import it.carmelolagamba.jcal.model.NeighborhoodType;

public class Main {

    public static int WIDTH = 10, HEIGHT = 10;

    public static DefaultStatus dead = new DefaultStatus("dead", "0");
    public static DefaultStatus alive = new DefaultStatus("alive", "1");
    public static List<DefaultStatus> status = Arrays.asList(dead, alive);
    public static List<DefaultCell> initalState = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        initalState.add(new DefaultCell(alive, 1, 1));
        initalState.add(new DefaultCell(alive, 1, 2));
        initalState.add(new DefaultCell(alive, 1, 3));
        initalState.add(new DefaultCell(alive, 2, 1));

        CellularAutomataConfigurationBuilder configBuilder = new CellularAutomataConfigurationBuilder();
        CellularAutomataConfiguration config = configBuilder.setHeight(WIDTH).setWidth(HEIGHT).setTotalIterations(500)
                .setDefaultStatus(Main.dead).setNeighborhoodType(NeighborhoodType.MOORE).setInitalState(initalState)
                /** ... */
                .build();

        CellularAutomata ca = new CellularAutomata(config);
        GOLExecutor executor = new GOLExecutor();
        ca = executor.run(ca);
        System.out.println(ca);
    }
}
