package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import org.eclipse.collections.api.map.primitive.IntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KnowledgeObject extends GameObject {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = KnowledgeObject.class.getSimpleName();

    public String type;

    public IntObjectMap<? extends GameObject> objects;

    @Override
    public String getObjectType() {
        return KnowledgeObject.OBJECT_TYPE;
    }
}
