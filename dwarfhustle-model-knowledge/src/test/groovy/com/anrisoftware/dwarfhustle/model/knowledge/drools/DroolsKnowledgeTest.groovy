package com.anrisoftware.dwarfhustle.model.knowledge.drools

import org.junit.jupiter.api.Test

class DroolsKnowledgeTest {

    @Test
    public void whenCriteriaMatching_ThenSuggestManagerRole(){
        Applicant applicant = new Applicant("David", 37, 1600000.0,11);
        SuggestedRole suggestedRole = new SuggestedRole();
        DroolsKnowledge drools = new DroolsKnowledge()
        def fileSystem = drools.getKieFileSystem()
        def session = drools.getKieSession(fileSystem)
        drools.suggestARoleForApplicant(session, applicant, suggestedRole);
        assert "Manager" == suggestedRole.role
    }
}
