package com.anrisoftware.dwarfhustle.model.knowledge.powerloom;

import java.util.Spliterator;
import java.util.function.Consumer;

import edu.isi.stella.List;
import edu.isi.stella.Stella_Object;

/**
 * Creates a {@link Spliterator} for a Stella {@link List} and Stella
 * {@link Stella_Object}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 * @since 0.1.0
 */
public class PowerLoomSpliteratorSupport {

    private final List list;

    public PowerLoomSpliteratorSupport(List list) {
        this.list = list;
    }

    public Spliterator<Stella_Object> spliterator() {
        return new PowerLoomSpliterator(list, 0, list.length());
    }

    static class PowerLoomSpliterator implements Spliterator<Stella_Object> {

        private final List list;

        private int origin; // current index, advanced on split or traversal

        private final int fence; // one past the greatest index

        PowerLoomSpliterator(List list, int origin, int fence) {
            this.list = list;
            this.origin = origin;
            this.fence = fence;
        }

        @Override
        public void forEachRemaining(Consumer<? super Stella_Object> action) {
            for (; origin < fence; origin += 1)
                action.accept(list.nth(origin));
        }

        @Override
        public boolean tryAdvance(Consumer<? super Stella_Object> action) {
            if (origin < fence) {
                action.accept(list.nth(origin));
                origin += 1;
                return true;
            } else // cannot advance
                return false;
        }

        @Override
        public Spliterator<Stella_Object> trySplit() {
            int lo = origin; // divide range in half
            int mid = ((lo + fence) >>> 1) & ~1; // force midpoint to be even
            if (lo < mid) { // split out left half
                origin = mid; // reset this Spliterator's origin
                return new PowerLoomSpliterator(list, lo, mid);
            } else // too small to split
                return null;
        }

        @Override
        public long estimateSize() {
            return (fence - origin) / 2;
        }

        @Override
        public int characteristics() {
            return ORDERED | SIZED | IMMUTABLE | SUBSIZED;
        }
    }
}