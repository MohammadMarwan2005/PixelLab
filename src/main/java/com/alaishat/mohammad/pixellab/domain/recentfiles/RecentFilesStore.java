package com.alaishat.mohammad.pixellab.domain.recentfiles;

import java.util.List;

/**
 * Persistence port for the recent-files list. The infrastructure layer provides
 * the on-disk JSON implementation; the use cases compose load → mutate → save
 * on top of this interface.
 */
public interface RecentFilesStore {
    List<RecentFile> load();
    void save(List<RecentFile> recents);
}
