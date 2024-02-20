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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import com.anrisoftware.dwarfhustle.model.actor.MainActor.MainActorFactory;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.Behaviors;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

/**
 * Provides the Akka actors system. It will create an actor system with the
 * {@link MainActor} actor.
 *
 * @author Erwin Müller
 */
@Singleton
@Slf4j
public class ActorSystemProvider implements Provider<ActorRef<Message>> {

    private final ActorSystem<Message> actors;

    private final MutableIntObjectMap<ObjectsGetter> ogs = IntObjectMaps.mutable.empty();

    private final MutableIntObjectMap<ObjectsSetter> oss = IntObjectMaps.mutable.empty();

    @Delegate
    private MainActor mainActor;

    @Inject
    public ActorSystemProvider(MainActorFactory mainFactory) {
        this.actors = ActorSystem.create(Behaviors.setup(context -> {
            mainActor = mainFactory.create(context);
            synchronized (ActorSystemProvider.this) {
                notify();
            }
            return mainActor;
        }), MainActor.class.getSimpleName());
    }

    public synchronized void waitMainActor() throws InterruptedException {
        while (mainActor == null) {
            wait(10);
        }
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

    public ActorSystem<Message> spawn(Behavior<Message> b, String name) {
        return ActorSystem.create(b, name);
    }

    public CompletionStage<Done> shutdown() {
        actors.terminate();
        return actors.getWhenTerminated();
    }

    public void shutdownWait() throws TimeoutException, InterruptedException {
        actors.terminate();
        Await.ready(actors.whenTerminated(), Duration.Inf());
    }

    public synchronized void registerObjectsGetter(int id, ObjectsGetter og) {
        log.trace("registerObjectsGetter {} {}", id, og);
        ogs.put(id, og);
    }

    public CompletionStage<ObjectsGetter> getObjectGetterAsync(int id) {
        return CompletableFuture.supplyAsync(() -> supplyObjectGetter(id));
    }

    public synchronized void registerObjectsSetter(int id, ObjectsSetter os) {
        log.trace("registerObjectsSetter {} {}", id, os);
        oss.put(id, os);
    }

    public CompletionStage<ObjectsSetter> getObjectSetterAsync(int id) {
        return CompletableFuture.supplyAsync(() -> supplyObjectSetter(id));
    }

    @SneakyThrows
    private ObjectsGetter supplyObjectGetter(int id) {
        ObjectsGetter og;
        while ((og = ogs.get(id)) == null) {
            Thread.sleep(10);
        }
        return og;
    }

    @SneakyThrows
    private ObjectsSetter supplyObjectSetter(int id) {
        ObjectsSetter os;
        while ((os = oss.get(id)) == null) {
            Thread.sleep(10);
        }
        return os;
    }
}
