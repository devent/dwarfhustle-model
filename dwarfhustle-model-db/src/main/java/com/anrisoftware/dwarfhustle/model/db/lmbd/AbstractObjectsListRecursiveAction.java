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