package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages;

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor.WORKING_MODULE;

import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;

import edu.isi.powerloom.PLI;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.stella.FloatWrapper;
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

}
