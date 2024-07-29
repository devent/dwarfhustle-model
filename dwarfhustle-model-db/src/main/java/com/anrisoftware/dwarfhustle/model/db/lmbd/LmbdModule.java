package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;

import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import jakarta.inject.Singleton;

/**
 * 
 */
public class LmbdModule extends AbstractModule {

    @Singleton
    @Provides
    public IntSet getObjectTypes() {
        MutableIntSet set = IntSets.mutable.empty();
        var loader = ServiceLoader.load(StoredObject.class);
        StreamSupport.stream(loader.spliterator(), true).forEach(s -> {
            set.add(s.getObjectType());
        });
        assertThat(set.size(), is(greaterThan(0)));
        return set;
    }

}
