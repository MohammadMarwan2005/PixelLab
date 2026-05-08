package com.alaishat.mohammad.pixellab.features.recentfiles.usecase;

import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFile;
import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFilesStore;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Records that a file was just opened. If the path is already present its
 * timestamp is updated and its UUID preserved (selection survives across opens).
 * Otherwise a new entry is added. The list is capped at {@link #MAX_RECENTS}.
 */
public final class AddRecentFileUseCase {

    public static final int MAX_RECENTS = 10;

    private final RecentFilesStore store;

    public AddRecentFileUseCase(RecentFilesStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public List<RecentFile> execute(Path path) {
        Path canonical = path.toAbsolutePath().normalize();
        Instant now = Instant.now();

        List<RecentFile> existing = new ArrayList<>(store.load());
        UUID id = UUID.randomUUID();
        for (int i = 0; i < existing.size(); i++) {
            RecentFile rf = existing.get(i);
            if (rf.path().toAbsolutePath().normalize().equals(canonical)) {
                id = rf.id();
                existing.remove(i);
                break;
            }
        }
        existing.add(new RecentFile(id, canonical, now));

        existing.sort(Comparator.comparing(RecentFile::lastOpenedAt).reversed());
        if (existing.size() > MAX_RECENTS) {
            existing.subList(MAX_RECENTS, existing.size()).clear();
        }
        store.save(existing);
        return List.copyOf(existing);
    }
}
