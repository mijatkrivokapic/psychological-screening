package com.ftn.sbnz.service.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

@Service
public class TemplateStorageService {

    public static final List<String> KINDS = List.of(
            "item_scoring",
            "subscale_classification",
            "composite_classification"
    );
    public static final List<String> TYPES = List.of("drt", "xlsx");

    private static final String ACTIVE_MARKER = "_active";
    private static final String INITIAL_VERSION = "default";

    @Value("${app.templates.dir}")
    private String templatesDir;

    @PostConstruct
    public void initFromClasspathIfMissing() throws IOException {
        Path base = Paths.get(templatesDir);
        Files.createDirectories(base);

        for (String kind : KINDS) {
            for (String type : TYPES) {
                Path channelDir = base.resolve(kind).resolve(type);
                if (Files.exists(channelDir)) continue;

                Files.createDirectories(channelDir);

                String classpathName = kind + "." + type;
                try (InputStream is = getClass().getResourceAsStream("/templatetable/" + classpathName)) {
                    if (is == null) {
                        throw new IllegalStateException("Default not on classpath: " + classpathName);
                    }
                    Files.copy(is, channelDir.resolve(INITIAL_VERSION));
                }
                Files.writeString(channelDir.resolve(ACTIVE_MARKER), INITIAL_VERSION);
            }
        }
    }


    public List<String> listVersions(String kind, String type) throws IOException {
        try (var stream = Files.list(channelDir(kind, type))) {
            return stream
                    .filter(p -> !p.getFileName().toString().startsWith("_"))
                    .map(p -> p.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }
    }

    public String getActiveVersion(String kind, String type) throws IOException {
        return Files.readString(channelDir(kind, type).resolve(ACTIVE_MARKER)).trim();
    }

    public InputStream openActive(String kind, String type) throws IOException {
        return openVersion(kind, type, getActiveVersion(kind, type));
    }

    public InputStream openVersion(String kind, String type, String version) throws IOException {
        Path file = versionPath(kind, type, version);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(kind + "/" + type + "/" + version);
        }
        return Files.newInputStream(file);
    }

    public void saveVersion(String kind, String type, String version, InputStream content) throws IOException {
        validateVersionName(version);
        Path target = versionPath(kind, type, version);
        Path tmp = target.resolveSibling(version + ".tmp");
        try {
            Files.copy(content, tmp, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tmp, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public void setActive(String kind, String type, String version) throws IOException {
        if (!Files.exists(versionPath(kind, type, version))) {
            throw new FileNotFoundException(kind + "/" + type + "/" + version);
        }
        Files.writeString(channelDir(kind, type).resolve(ACTIVE_MARKER), version);
    }

    public void deleteVersion(String kind, String type, String version) throws IOException {
        if (version.equals(getActiveVersion(kind, type))) {
            throw new IllegalStateException(
                    "Cannot delete active version. Activate another version first.");
        }
        Files.deleteIfExists(versionPath(kind, type, version));
    }


    private Path channelDir(String kind, String type) {
        if (!KINDS.contains(kind)) {
            throw new IllegalArgumentException("Unknown kind: " + kind);
        }
        if (!TYPES.contains(type)) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return Paths.get(templatesDir, kind, type);
    }

    private Path versionPath(String kind, String type, String version) {
        validateVersionName(version);
        return channelDir(kind, type).resolve(version);
    }

    private void validateVersionName(String name) {
        if (name == null || name.isBlank() || name.startsWith("_") ||
                name.contains("/") || name.contains("\\") || name.contains("..")) {
            throw new IllegalArgumentException("Invalid version name: " + name);
        }
    }
}