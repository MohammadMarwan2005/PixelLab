package com.alaishat.mohammad.pixellab.features.recentfiles.usecase;

import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFile;
import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFilesStore;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class LoadRecentFilesUseCase {

    private final RecentFilesStore store;

    public LoadRecentFilesUseCase(RecentFilesStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public List<RecentFile> execute() {
        return store.load().stream()
                .sorted(Comparator.comparing(RecentFile::lastOpenedAt).reversed())
                .toList();
    }
}
