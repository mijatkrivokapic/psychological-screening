package com.ftn.sbnz.service.service;

import com.ftn.sbnz.model.*;

import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Collection;

@Service
public class MinimalTestService {

    private final KieServices kieServices = KieServices.Factory.get();

    @PostConstruct
    public void testUcitavanjaPravila() {
        System.out.println("--- POKRETANJE DROOLS TESTA ---");

        try {
            ExternalSpreadsheetCompiler converter = new ExternalSpreadsheetCompiler();

            InputStream scoringTpl  = getClass().getResourceAsStream("/templatetable/item_scoring.drt");
            InputStream scoringData = getClass().getResourceAsStream("/templatetable/item_scoring.xlsx");
            if (scoringTpl == null || scoringData == null) {
                throw new RuntimeException("Scoring template ili xlsx nije pronađen!");
            }
            String scoringDrl = converter.compile(scoringData, scoringTpl, 2, 1);

// --- 2. Klasifikacija (novo) ---
            InputStream classifTpl  = getClass().getResourceAsStream("/templatetable/subscale_classification.drt");
            InputStream classifData = getClass().getResourceAsStream("/templatetable/subscale_classification.xlsx");
            if (classifTpl == null || classifData == null) {
                throw new RuntimeException("Classification template ili xlsx nije pronađen!");
            }
            String classificationDrl = converter.compile(classifData, classifTpl, 2, 1);

            InputStream compositeTpl  = getClass().getResourceAsStream("/templatetable/composite_classification.drt");
            InputStream compositeData = getClass().getResourceAsStream("/templatetable/composite_classification.xlsx");
            if (compositeTpl == null || compositeData == null) {
                throw new RuntimeException("Composite template ili xlsx nije pronađen!");
            }
            String compositeDrl = converter.compile(compositeData, compositeTpl, 2, 1);

            System.out.println("--- GENERISANI SCORING DRL ---");
            System.out.println(scoringDrl);
            System.out.println("--- GENERISANI CLASSIFICATION DRL ---");
            System.out.println(classificationDrl);
            System.out.println("--- GENERISANI COMPOSITE DRL ---");
            System.out.println(compositeDrl);

            KieSession ksession = createKieSession(scoringDrl, classificationDrl, compositeDrl);


            // ===== UBACIVANJE ČINJENICA =====
            String familyId = "family_123";

            // Po jedan odgovor po item-u (vrednosti su izmišljene, prilagodi po potrebi)
            for (int i : new int[]{1, 7, 10, 11, 12, 18}) {
                ksession.insert(new Answer(familyId, i, 5));
            }

            // disability_support → svih 4 = 1 → prosek 1.0 → LOW
            for (int i : new int[]{22, 23, 24, 25}) {
                ksession.insert(new Answer(familyId, i, 1));
            }

            // ostatak (parenting, emotional_wellbeing, physical_material_wellbeing) → 3 → MODERATE
            for (int i : new int[]{2, 5, 8, 14, 17, 19, 3, 4, 9, 13, 6, 15, 16, 20, 21}) {
                ksession.insert(new Answer(familyId, i, 3));
            }

            // ===== OKIDANJE PRAVILA =====
            int firedRules = ksession.fireAllRules();
            System.out.println("Broj okinutih pravila: " + firedRules);

            // ===== ČITANJE REZULTATA IZ RADNE MEMORIJE =====

            Collection<?> scored = ksession.getObjects(o -> o instanceof ScoredItem);
            System.out.println("--- ScoredItem (" + scored.size() + ") ---");
            scored.forEach(System.out::println);

            Collection<?> averages = ksession.getObjects(o -> o instanceof SubscaleAverage);
            System.out.println("--- SubscaleAverage (" + averages.size() + ") ---");
            averages.forEach(System.out::println);

            Collection<?> categories = ksession.getObjects(o -> o instanceof CategorizedSubscale);
            System.out.println("--- CategorizedSubscale (" + categories.size() + ") ---");
            categories.forEach(System.out::println);

            Collection<?> composites = ksession.getObjects(o -> o instanceof CategorizedComposite);
            System.out.println("--- CategorizedComposite (" + composites.size() + ") ---");
            composites.forEach(System.out::println);

            ksession.dispose();

        } catch (Exception e) {
            System.err.println("Došlo je do greške prilikom učitavanja: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private KieSession createKieSession(String scoringDrl,
                                        String classificationDrl,
                                        String compositeDrl) {
        KieHelper kieHelper = new KieHelper();

        kieHelper.addContent(scoringDrl,        ResourceType.DRL);
        kieHelper.addContent(classificationDrl, ResourceType.DRL);
        kieHelper.addContent(compositeDrl,      ResourceType.DRL);

        kieHelper.addResource(
                kieServices.getResources().newClassPathResource("rules/aggregate_subscales.drl"),
                ResourceType.DRL
        );

        Results results = kieHelper.verify();
        if (results.hasMessages(Message.Level.WARNING, Message.Level.ERROR)) {
            for (Message m : results.getMessages(Message.Level.WARNING, Message.Level.ERROR)) {
                System.out.println("Error: " + m.getText());
            }
            throw new IllegalStateException("Compilation errors were found. Check the logs.");
        }

        return kieHelper.build().newKieSession();
    }
}