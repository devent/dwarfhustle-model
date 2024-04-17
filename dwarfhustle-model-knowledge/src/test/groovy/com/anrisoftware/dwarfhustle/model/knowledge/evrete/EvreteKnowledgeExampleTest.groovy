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
package com.anrisoftware.dwarfhustle.model.knowledge.evrete

import org.evrete.KnowledgeService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest

import groovy.util.logging.Slf4j

/**
 * <pre>
 * 
 [ruleset_declaration]   Fire    knowledge               Time   Average Median                       
 [ruleset_declaration]   Finish  fire    knowledge       840         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       485         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       461         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       195         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       263         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       221         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       231         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       261         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       176         
 [ruleset_declaration]   Fire    knowledge                       
 [ruleset_declaration]   Finish  fire    knowledge       263     339.6   262
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       410         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       372         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       297         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       212         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       238         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       310         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       420         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       232         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       236         
 [annotated_java_rules]  Fire    knowledge                       
 [annotated_java_rules]  Finish  fire    knowledge       190     291.7   267.5
 * </pre>
 */
@Slf4j
class EvreteKnowledgeExampleTest {

    static List customers

    static List sessionData

    @BeforeAll
    static void setUp() {
        this.customers = Arrays.asList(
                new Customer("Customer A"),
                new Customer("Customer B"),
                new Customer("Customer C")
                );

        Random random = new Random();
        this.sessionData = new LinkedList<>(customers);
        for (int i = 0; i < 100_000; i++) {
            Customer randomCustomer = customers.get(random.nextInt(customers.size()));
            Invoice invoice = new Invoice(randomCustomer, 100 * random.nextDouble());
            sessionData.add(invoice);
        }
        log.debug "Done setup"
    }

    @RepeatedTest(1)
    void ruleset_declaration() {
        def knowledge = new EvreteKnowledgeExample().createKnowledge()
        log.debug "[ruleset_declaration] Fire knowledge"
        knowledge
                .newStatelessSession()
                .insert(sessionData)
                .fire();
        log.debug "[ruleset_declaration] Finish fire knowledge"
        customers.each {
            println "${it.getName()} ${it.getTotal()}"
        }
    }

    @RepeatedTest(1)
    void annotated_java_rules() {
        def service = new KnowledgeService();
        URL rulesetUrl = EvreteKnowledgeExampleTest.class.getResource("SalesRuleset.java")
        def knowledge = service.newKnowledge(
                "JAVA-SOURCE",
                rulesetUrl
                );
        log.debug "[annotated_java_rules] Fire knowledge"
        knowledge.newStatelessSession()
                .insert(sessionData)
                .fire()
        log.debug "[annotated_java_rules] Finish fire knowledge"
        customers.each {
            println "${it.getName()} ${it.getTotal()}"
        }
    }
}
