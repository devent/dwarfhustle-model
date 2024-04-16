package com.anrisoftware.dwarfhustle.model.knowledge.drools;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.replace;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

public class DroolsKnowledge {

    KieServices kieServices = KieServices.Factory.get();

    public KieFileSystem getKieFileSystem() {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        var rules = asList(format("%s/%s", replace(getClass().getPackageName(), ".", "/"), "SuggestApplicant.drl"));
        for (String rule : rules) {
            kieFileSystem.write(ResourceFactory.newClassPathResource(rule));
        }
        return kieFileSystem;
    }

    public KieRepository getKieRepository() {
        final KieRepository kieRepository = kieServices.getRepository();
        kieRepository.addKieModule(kieRepository::getDefaultReleaseId);
        return kieRepository;
    }

    public KieSession getKieSession(KieFileSystem fileSystem) {
        KieBuilder kb = kieServices.newKieBuilder(fileSystem);
        kb.buildAll();

        KieRepository kieRepository = kieServices.getRepository();
        ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
        KieContainer kieContainer = kieServices.newKieContainer(krDefaultReleaseId);

        return kieContainer.newKieSession();
    }

    public SuggestedRole suggestARoleForApplicant(KieSession kieSession, Applicant applicant,
            SuggestedRole suggestedRole) {
        try {
            kieSession.insert(applicant);
            kieSession.setGlobal("suggestedRole", suggestedRole);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }
        System.out.println(suggestedRole.getRole());
        return suggestedRole;
    }
}
