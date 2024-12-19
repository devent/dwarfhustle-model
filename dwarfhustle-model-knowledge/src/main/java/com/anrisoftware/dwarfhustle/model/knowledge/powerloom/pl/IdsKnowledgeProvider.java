/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.lable.oss.uniqueid.IDGenerator;
import org.lable.oss.uniqueid.LocalUniqueIDGeneratorFactory;
import org.lable.oss.uniqueid.bytes.Mode;

import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeLoadedObject;
import com.google.inject.BindingAnnotation;

import jakarta.inject.Provider;
import jakarta.inject.Qualifier;

/**
 * Provides a Id generator for {@link KnowledgeLoadedObject} knowledge loaded
 * objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class IdsKnowledgeProvider implements Provider<IDGenerator> {

    @Qualifier
    @Target({ FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME)
    @BindingAnnotation
    public @interface IdsKnowledge {
    }

    public static final int GENERATOR_ID = 0;

    public static final int CLUSTER_ID = 2;

    private final IDGenerator generator;

    public IdsKnowledgeProvider() {
        this.generator = LocalUniqueIDGeneratorFactory.generatorFor(GENERATOR_ID, CLUSTER_ID, Mode.SPREAD);
    }

    @Override
    public IDGenerator get() {
        return generator;
    }

}
