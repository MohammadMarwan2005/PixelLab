package com.alaishat.mohammad.pixellab.features.recentfiles.viewmodel;

import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFile;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.AddRecentFileUseCase;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.LoadRecentFilesUseCase;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.RemoveRecentFileUseCase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RecentFilesViewModel {

    private final LoadRecentFilesUseCase load;
    private final AddRecentFileUseCase add;
    private final RemoveRecentFileUseCase remove;

    private final ObservableList<RecentFile> recents = FXCollections.observableArrayList();
    private final ObservableList<RecentFile> recentsView = FXCollections.unmodifiableObservableList(recents);

    public RecentFilesViewModel(LoadRecentFilesUseCase load,
                                AddRecentFileUseCase add,
                                RemoveRecentFileUseCase remove) {
        this.load = Objects.requireNonNull(load, "load");
        this.add = Objects.requireNonNull(add, "add");
        this.remove = Objects.requireNonNull(remove, "remove");
    }

    public ObservableList<RecentFile> recents() {
        return recentsView;
    }

    public void refresh() {
        replaceAll(load.execute());
    }

    public void recordOpened(Path path) {
        replaceAll(add.execute(path));
    }

    public void removeById(UUID id) {
        replaceAll(remove.execute(id));
    }

    private void replaceAll(List<RecentFile> next) {
        recents.setAll(next);
    }
}
