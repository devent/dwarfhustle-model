package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import org.eclipse.collections.api.list.ListIterable;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Stores loaded knowledge.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KnowledgeObject extends GameObject {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = KnowledgeObject.class.getSimpleName();

    public String type;

    public ListIterable<GameObject> objects;

    @Override
    public String getObjectType() {
        return KnowledgeObject.OBJECT_TYPE;
    }
}
