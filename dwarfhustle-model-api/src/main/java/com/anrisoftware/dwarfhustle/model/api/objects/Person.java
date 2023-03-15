/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Person on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class Person extends GameMovingObject {

    private static final long serialVersionUID = -6027695018525898404L;

    public static final String OBJECT_TYPE = Person.class.getSimpleName();

    private String firstName;

    private String secondName;

    private String lastName;

    public Person(long id) {
        super(id);
    }

    public Person(byte[] idbuf) {
        super(idbuf);
    }

    @Override
    public String getObjectType() {
        return OBJECT_TYPE;
    }

    public void setFirstName(String firstName) {
        if (this.firstName != firstName) {
            setDirty(true);
            this.firstName = firstName;
        }
    }

    public void setSecondName(String secondName) {
        if (this.secondName != secondName) {
            setDirty(true);
            this.secondName = secondName;
        }
    }

    public void setLastName(String lastName) {
        if (this.lastName != lastName) {
            setDirty(true);
            this.lastName = lastName;
        }
    }

}
