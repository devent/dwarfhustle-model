/*
 * dwarfhustle-model-actor - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.actor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.anrisoftware.dwarfhustle.model.actor.MainActor.MainActorFactory;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Provides the Akka actors system. It will create an actor system with the
 * {@link MainActor} actor.
 *
 * @author Erwin Müller
 */
@Singleton
public class ActorSystemProvider implements Provider<ActorRef<Message>> {

    private final ActorSystem<Message> actors;

    private MainActor mainActor;

    @Inject
    public ActorSystemProvider(MainActorFactory mainFactory) {
        this.actors = ActorSystem.create(Behaviors.setup((context) -> {
            mainActor = mainFactory.create(context);
            return mainActor;
		}), MainActor.class.getSimpleName());
    }

    public MainActor getMainActor() {
        return mainActor;
    }

    public ActorSystem<Message> getActorSystem() {
        return actors;
    }

    @Override
    public ActorRef<Message> get() {
        return actors;
    }

	public void tell(Message m) {
		mainActor.tell(m);
	}

	public Scheduler getScheduler() {
		return actors.scheduler();
	}

}
