/*
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 * Released as open-source under the Apache License, Version 2.0.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model
 * ****************************************************************************
 *
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model is a derivative work based on Josua Tippetts' C++ library:
 * http://accidentalnoise.sourceforge.net/index.html
 * ****************************************************************************
 *
 * Copyright (C) 2011 Joshua Tippetts
 *
 *   This software is provided 'as-is', without any express or implied
 *   warranty.  In no event will the authors be held liable for any damages
 *   arising from the use of this software.
 *
 *   Permission is granted to anyone to use this software for any purpose,
 *   including commercial applications, and to alter it and redistribute it
 *   freely, subject to the following restrictions:
 *
 *   1. The origin of this software must not be misrepresented; you must not
 *      claim that you wrote the original software. If you use this software
 *      in a product, an acknowledgment in the product documentation would be
 *      appreciated but is not required.
 *   2. Altered source versions must be plainly marked as such, and must not be
 *      misrepresented as being the original software.
 *   3. This notice may not be removed or altered from any source distribution.
 *
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model bundles and uses the RandomCL library:
 * https://github.com/bstatcomp/RandomCL
 * ****************************************************************************
 *
 * BSD 3-Clause License
 *
 * Copyright (c) 2018, Tadej Ciglarič, Erik Štrumbelj, Rok Češnovar. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.anrisoftware.dwarfhustle.model.db.cache;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongSets;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.GameMapPosition;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.google.inject.Injector;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class MapTilesJcsCacheActor extends AbstractJcsCacheActor<GameMapPosition, MapTile> {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
			MapTilesJcsCacheActor.class.getSimpleName());

	public static final String NAME = MapTilesJcsCacheActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	/**
	 * Factory to create {@link MapTilesJcsCacheActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface MapTilesJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

		@Override
		MapTilesJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
				Map<String, Object> params);
	}

	public static <K, V> Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
			CompletionStage<CacheAccess<K, V>> initCacheAsync, Map<String, Object> params) {
		return AbstractJcsCacheActor.create(injector, actorFactory, initCacheAsync, params);
	}

	/**
	 * Creates the {@link MapTilesJcsCacheActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
			Map<String, Object> params) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		var actorFactory = injector.getInstance(MapTilesJcsCacheActorFactory.class);
		var initCache = createInitCacheAsync(params);
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, initCache, params));
	}

	public static CompletableFuture<CacheAccess<Object, Object>> createInitCacheAsync(Map<String, Object> params) {
		var initCache = CompletableFuture.supplyAsync(() -> {
			return createCache(params);
		});
		return initCache;
	}

	private static CacheAccess<Object, Object> createCache(Map<String, Object> params) {
		try {
			var mapid = params.get("mapid");
			params.put("cache_name", "mapTilesCache_" + mapid);
			params.put("max_objects", 32768);
			params.put("is_enternal", true);
			var config = new Properties();
			createFileAuxCache(config, params);
			config.put("jcs.region.mapTilesCache", "file");
			createCache(config, params);
			JCS.setConfigProperties(config);
			return JCS.getInstance("mapTilesCache");
		} catch (CacheException e) {
			throw new RuntimeException(e);
		}
	}

	private int mapid;

	private int width;

	private int height;

	private int depth;

	private MutableLongSet ids;

	@Override
	protected Behavior<Message> initialStage(InitialStateMessage<GameMapPosition, MapTile> m) {
		log.debug("initialStage {}", m);
		this.mapid = (int) params.get("mapid");
		this.width = (int) params.get("width");
		this.height = (int) params.get("height");
		this.depth = (int) params.get("depth");
		this.ids = LongSets.mutable.withInitialCapacity(width * height * depth);
		return super.initialStage(m);
	}

	@Override
	protected MapTile retrieveValueFromDb(GetMessage m) {
		return null;
	}

	@Override
	protected void storeValueDb(PutMessage m) {
	}
}
