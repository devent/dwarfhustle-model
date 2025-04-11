/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages;

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor.WORKING_MODULE;

import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;

import edu.isi.powerloom.PLI;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.stella.Cons;
import edu.isi.stella.FloatWrapper;
import edu.isi.stella.IntegerWrapper;
import edu.isi.stella.List;
import edu.isi.stella.Stella_Object;

/**
 *
 */
public class PowerLoomUtils {

    /**
     * Retrieves a Float.
     */
    public static float retrieveFloat(String function, String name) {
        var buff = new StringBuilder();
        buff.append("?x (");
        buff.append(function);
        buff.append(" ");
        buff.append(name);
        buff.append(" ?x)");
        var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
        FloatWrapper next;
        while ((next = (FloatWrapper) answer.pop()) != null) {
            return (float) next.wrapperValue;
        }
        return -1;
    }

    /**
     * Retrieves a String.
     */
    public static String retrieveString(String function, String name) {
        var buff = new StringBuilder();
        buff.append("?x (");
        buff.append(function);
        buff.append(" ");
        buff.append(name);
        buff.append(" ?x)");
        var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
        LogicObject next;
        while ((next = (LogicObject) answer.pop()) != null) {
            return next.surrogateValueInverse.symbolName;
        }
        return null;
    }

    /**
     * Retrieves a Integer.
     */
    public static int retrieveInt(String function, String name) {
        var buff = new StringBuilder();
        buff.append("?x (");
        buff.append(function);
        buff.append(" ");
        buff.append(name);
        buff.append(" ?x)");
        var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
        IntegerWrapper next;
        while ((next = (IntegerWrapper) answer.pop()) != null) {
            return next.wrapperValue;
        }
        return -1;
    }

    /**
     * Retrieves a set of integer.
     */
    public static IntSet retrieveIntSet(String function, String selector, MutableIntSet store) {
        var buff = new StringBuilder();
        buff.append("all (");
        buff.append(function);
        buff.append(" ");
        buff.append(selector);
        buff.append(" ?x)");
        var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
        List list = answer.listify();
        Stella_Object next;
        while ((next = list.pop()) != null) {
            if (next instanceof LogicObject lo) {
                store.add(lo.surrogateValueInverse.symbolId);
            }
        }
        return store;
    }

    /**
     * Retrieves the id of the object returned by the function.
     */
    public static int retrieveIdFunc(String function, String name) {
        var buff = new StringBuilder();
        buff.append("?x (");
        buff.append(function);
        buff.append(" ");
        buff.append(name);
        buff.append(" ?x)");
        var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
        LogicObject next;
        while ((next = (LogicObject) answer.pop()) != null) {
            return next.surrogateValueInverse.symbolId;
        }
        return -1;
    }

    /**
     * Retrieves the (object, int) mappings from a function.
     */
    public static IntIntMap retrieveIdIntFunc(String function, String name, MutableIntIntMap store) {
        var buff = new StringBuilder();
        buff.append("all (");
        buff.append(function);
        buff.append(" ");
        buff.append(name);
        buff.append(" ?x ?y)");
        var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
        Cons next;
        while ((next = (Cons) answer.pop()) != null) {
            store.put(((LogicObject) next.value).surrogateValueInverse.symbolId,
                    ((IntegerWrapper) next.rest.value).wrapperValue);
        }
        return store;
    }

    public static IntSet retrieveIdFunc(String function, String name, MutableIntSet store) {
        var buff = new StringBuilder();
        buff.append("all (");
        buff.append(function);
        buff.append(" ");
        buff.append(name);
        buff.append(" ?x)");
        var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
        LogicObject next;
        while ((next = (LogicObject) answer.pop()) != null) {
            store.add(next.surrogateValueInverse.symbolId);
        }
        return store;
    }

}
