package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import lombok.RequiredArgsConstructor;

/**
 * Exception that the object was not found in the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
public class ObjectNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public final String objectType;
}
