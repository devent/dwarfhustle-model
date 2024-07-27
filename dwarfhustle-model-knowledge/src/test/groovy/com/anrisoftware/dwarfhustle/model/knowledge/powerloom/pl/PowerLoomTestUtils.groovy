/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl

import java.util.stream.StreamSupport

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseErrorMessage
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeResponseSuccessMessage

import akka.actor.typed.javadsl.Behaviors
import edu.isi.powerloom.Environment
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator
import edu.isi.powerloom.logic.TruthValue
import groovy.util.logging.Slf4j

/**
 * Utils to query PowerLoom.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class PowerLoomTestUtils {

    static void printPowerLoomRetrieve(String query, String module, Environment env = null) {
        PlIterator answer = PLI.sRetrieve(query, module, env);
        printSeparator();
        println "Answers to query `$query`"
        StreamSupport.stream(new PowerLoomSpliteratorSupport(answer.listify()).spliterator(), false).forEach({ v -> println v })
        printSeparator();
    }

    static void printPowerLoomAsk(String query, String module, Environment env = null) {
        TruthValue answer = PLI.sAsk(query, module, env);
        printSeparator();
        println "Answers to query `$query`"
        println answer
        printSeparator();
    }

    static void printSeparator() {
        println "-----------------------------------------"
    }

    private static class WrappedKnowledgeResponse extends Message {
        KnowledgeResponseMessage response
        WrappedKnowledgeResponse(KnowledgeResponseMessage response) {
            this.response = response
        }
    }

    static def spawnListKnowledgeActor(ActorSystemProvider actor, def successCallback) {
        def knowledgeResponseAdapter
        def listKnowledge = actor.spawn(Behaviors.setup({ context ->
            knowledgeResponseAdapter = context.messageAdapter(KnowledgeResponseMessage.class, {
                new WrappedKnowledgeResponse(it)
            });
            return Behaviors.receive(Message.class)//
                    .onMessage(WrappedKnowledgeResponse.class, {
                        switch (it.response) {
                            case KnowledgeResponseSuccessMessage:
                                successCallback(it)
                                break
                            case KnowledgeResponseErrorMessage:
                                log.error("KnowledgeResponseErrorMessage", it.response.error)
                                break
                        }
                        Behaviors.same()
                    })//
                    .build()
        }), "listKnowledge")
        println listKnowledge
        while (knowledgeResponseAdapter == null) {
            Thread.sleep(10)
        }
        println knowledgeResponseAdapter
        while (listKnowledge == null) {
            Thread.sleep(10)
        }
        return [
            listKnowledge,
            knowledgeResponseAdapter,
        ]
    }
}
