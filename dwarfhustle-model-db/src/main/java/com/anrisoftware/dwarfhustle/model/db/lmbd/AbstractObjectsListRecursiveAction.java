/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.lmbd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

import lombok.RequiredArgsConstructor;

/**
 * Divides a list equally.
 */
@RequiredArgsConstructor
public abstract class AbstractObjectsListRecursiveAction extends RecursiveAction {

    private static final long serialVersionUID = 1L;

    protected final int max;

    protected final int start;

    protected final int end;

    protected final List<GameMapObject> objects;

    @Override
    protected void compute() {
        if (end - start > max) {
            ForkJoinTask.invokeAll(createSubtasks());
        } else {
            processing();
        }
    }

    private Collection<AbstractObjectsListRecursiveAction> createSubtasks() {
        List<AbstractObjectsListRecursiveAction> dividedTasks = new ArrayList<>();
        dividedTasks.add(create(max, start, start / 2 + end / 2, objects));
        dividedTasks.add(create(max, start / 2 + end / 2, end, objects));
        return dividedTasks;
    }

    protected abstract void processing();

    protected abstract AbstractObjectsListRecursiveAction create(int max, int start, int end,
            List<GameMapObject> objects);

}