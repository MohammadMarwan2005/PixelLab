package com.alaishat.mohammad.pixellab.features.recentfiles.usecase;

import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFile;
import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFilesStore;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RemoveRecentFileUseCase {

    private final RecentFilesStore store;

    public RemoveRecentFileUseCase(RecentFilesStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public List<RecentFile> execute(UUID id) {
        List<RecentFile> remaining = store.load().stream()
                .filter(rf -> !rf.id().equals(id))
                .toList();
        store.save(remaining);
        return remaining;
    }
}
