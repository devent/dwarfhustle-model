package com.anrisoftware.dwarfhustle.model.db.cache;

import org.apache.commons.jcs3.engine.CacheElement;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.GameObject;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to handle a cache event.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class CacheElementEventMessage extends Message {

	public final CacheElement<Object, GameObject> e;

	public final GameObject val;
}
