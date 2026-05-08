package com.alaishat.mohammad.pixellab.domain.recentfiles;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * One entry in the recent-files history. {@code id} is stable across renames so
 * the UI can identify rows for selection / removal even when the path changes.
 */
public record RecentFile(UUID id, Path path, Instant lastOpenedAt) {
    public static RecentFile freshlyOpened(Path path) {
        return new RecentFile(UUID.randomUUID(), path, Instant.now());
    }
}
