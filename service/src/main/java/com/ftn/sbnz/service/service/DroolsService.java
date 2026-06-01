package com.ftn.sbnz.service.service;

import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.kie.api.KieServices;
import org.kie.api.KieBase;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Service
public class DroolsService {

    private final KieServices kieServices = KieServices.Factory.get();
    private final TemplateStorageService storage;
    private volatile KieBase kieBase;

    public DroolsService(TemplateStorageService storage) {
        this.storage = storage;
    }

    @PostConstruct
    public void init() {
        rebuild();
    }

    public synchronized void rebuild() {
        try {
            ExternalSpreadsheetCompiler converter = new ExternalSpreadsheetCompiler();

            String scoringDrl        = compile(converter, "item_scoring");
            String classificationDrl = compile(converter, "subscale_classification");
            String compositeDrl      = compile(converter, "composite_classification");

            KieHelper kieHelper = new KieHelper();
            kieHelper.addContent(scoringDrl,        ResourceType.DRL);
            kieHelper.addContent(classificationDrl, ResourceType.DRL);
            kieHelper.addContent(compositeDrl,      ResourceType.DRL);

            kieHelper.addResource(
                    kieServices.getResources().newClassPathResource("rules/aggregate_subscales.drl"),
                    ResourceType.DRL);
            kieHelper.addResource(
                    kieServices.getResources().newClassPathResource("rules/cause_query.drl"),
                    ResourceType.DRL);

            Results results = kieHelper.verify();
            if (results.hasMessages(Message.Level.ERROR)) {
                throw new IllegalStateException("Compilation errors: " + results);
            }

            this.kieBase = kieHelper.build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to rebuild rules: " + e.getMessage(), e);
        }
    }

    private String compile(ExternalSpreadsheetCompiler converter, String kind) throws Exception {
        try (InputStream tpl  = storage.openActive(kind, "drt");
             InputStream data = storage.openActive(kind, "xlsx")) {
            return converter.compile(data, tpl, 2, 1);
        }
    }

    public KieSession newSession() {
        KieBase kb = this.kieBase;
        if (kb == null) throw new IllegalStateException("Rules not initialized.");
        return kb.newKieSession();
    }
}