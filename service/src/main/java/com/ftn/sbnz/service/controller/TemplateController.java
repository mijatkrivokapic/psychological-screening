package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.service.DroolsService;
import com.ftn.sbnz.service.service.TemplateStorageService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateStorageService storage;
    private final DroolsService drools;

    public TemplateController(TemplateStorageService storage, DroolsService drools) {
        this.storage = storage;
        this.drools = drools;
    }

    @GetMapping
    public List<Map<String, Object>> overview() throws IOException {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String kind : TemplateStorageService.KINDS) {
            for (String type : TemplateStorageService.TYPES) {
                out.add(Map.of(
                        "kind", kind,
                        "type", type,
                        "active", storage.getActiveVersion(kind, type),
                        "versions", storage.listVersions(kind, type)
                ));
            }
        }
        return out;
    }

    @GetMapping("/{kind}/{type}/versions")
    public Map<String, Object> versions(@PathVariable String kind,
                                        @PathVariable String type) throws IOException {
        return Map.of(
                "active", storage.getActiveVersion(kind, type),
                "versions", storage.listVersions(kind, type)
        );
    }

    @GetMapping("/{kind}/{type}/versions/{version}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String kind,
                                                        @PathVariable String type,
                                                        @PathVariable String version) throws IOException {
        String filename = kind + "_" + version + "." + type;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(storage.openVersion(kind, type, version)));
    }

    @PostMapping("/{kind}/{type}/versions/{version}")
    public ResponseEntity<?> upload(@PathVariable String kind,
                                    @PathVariable String type,
                                    @PathVariable String version,
                                    @RequestParam("file") MultipartFile file) throws IOException {
        boolean overwritingActive = version.equals(storage.getActiveVersion(kind, type));

        byte[] backup = null;
        if (overwritingActive) {
            try (var is = storage.openActive(kind, type)) {
                backup = is.readAllBytes();
            }
        }

        storage.saveVersion(kind, type, version, file.getInputStream());

        if (overwritingActive) {
            try {
                drools.rebuild();
            } catch (Exception e) {
                storage.saveVersion(kind, type, version, new java.io.ByteArrayInputStream(backup));
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                        "status", "error",
                        "message", "Compilation failed; version " + version + " rolled back. Reason: " + e.getMessage()
                ));
            }
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "kind", kind, "type", type, "version", version,
                "appliedNow", overwritingActive
        ));
    }

    @PutMapping("/{kind}/{type}/active")
    public ResponseEntity<?> activate(@PathVariable String kind,
                                      @PathVariable String type,
                                      @RequestBody Map<String, String> body) throws IOException {
        String newVersion = body.get("version");
        if (newVersion == null || newVersion.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'version' in body"));
        }

        String previous = storage.getActiveVersion(kind, type);
        if (newVersion.equals(previous)) {
            return ResponseEntity.ok(Map.of("status", "noop", "active", previous));
        }

        storage.setActive(kind, type, newVersion);
        try {
            drools.rebuild();
        } catch (Exception e) {
            storage.setActive(kind, type, previous);  // rollback markera
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "status", "error",
                    "message", "Compilation failed with " + newVersion + "; reverted to " + previous,
                    "reason", e.getMessage()
            ));
        }
        return ResponseEntity.ok(Map.of("status", "ok", "active", newVersion, "previous", previous));
    }

    @DeleteMapping("/{kind}/{type}/versions/{version}")
    public ResponseEntity<?> delete(@PathVariable String kind,
                                    @PathVariable String type,
                                    @PathVariable String version) throws IOException {
        try {
            storage.deleteVersion(kind, type, version);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }
}