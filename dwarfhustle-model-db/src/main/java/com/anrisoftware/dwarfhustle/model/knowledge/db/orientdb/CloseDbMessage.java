package com.anrisoftware.dwarfhustle.model.knowledge.db.orientdb;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to close the OrientDb database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CloseDbMessage extends Message {

}
