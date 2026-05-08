package com.alaishat.mohammad.pixellab.infrastructure.persistence;

import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFile;
import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFilesStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-backed recent-files store at {@code ~/.pixellab/recent.json}.
 *
 * Reads return an empty list if the file is missing or malformed (the user has
 * never opened anything yet, or the file was hand-edited). Writes go through a
 * temp file + atomic rename so a partial write can't leave the JSON corrupt.
 */
public final class JsonRecentFilesStore implements RecentFilesStore {

    private final Path filePath;
    private final ObjectMapper mapper;

    public JsonRecentFilesStore() {
        this(defaultFilePath());
    }

    public JsonRecentFilesStore(Path filePath) {
        this.filePath = filePath;
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<RecentFile> load() {
        if (!Files.exists(filePath)) return List.of();
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            if (bytes.length == 0) return List.of();
            return mapper.readValue(bytes, new TypeReference<List<RecentFile>>() {});
        } catch (IOException e) {
            // Corrupt or unreadable — treat as empty rather than crash.
            return List.of();
        }
    }

    @Override
    public void save(List<RecentFile> recents) {
        try {
            Files.createDirectories(filePath.getParent());
            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            mapper.writeValue(tmp.toFile(), new ArrayList<>(recents));
            try {
                Files.move(tmp, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path defaultFilePath() {
        return Paths.get(System.getProperty("user.home"), ".pixellab", "recent.json");
    }
}
