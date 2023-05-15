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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import java.util.function.Consumer;
import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to load a {@link GameObject} from the database. Responds with either
 * {@link LoadObjectSuccessMessage} or {@link DbErrorMessage}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
public class LoadObjectMessage<T extends DbMessage<?>> extends DbMessage<T> {

    @RequiredArgsConstructor
    public static class LoadObjectSuccessMessage<T extends DbMessage<?>> extends DbSuccessMessage<T> {
        public final GameObject go;
    }

    private static final Consumer<GameObject> EMPTY_CONSUMER = go -> {
    };

    public final String objectType;

    public final Consumer<GameObject> consumer;

    public final Function<ODatabaseDocument, OResultSet> query;

    public LoadObjectMessage(ActorRef<T> replyTo, String objectType, Function<ODatabaseDocument, OResultSet> query) {
        this(replyTo, objectType, EMPTY_CONSUMER, query);
    }

    public LoadObjectMessage(ActorRef<T> replyTo, String objectType, Consumer<GameObject> consumer,
            Function<ODatabaseDocument, OResultSet> query) {
        super(replyTo);
        this.objectType = objectType;
        this.consumer = consumer;
        this.query = query;
    }

}
