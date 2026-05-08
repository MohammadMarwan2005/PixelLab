package com.alaishat.mohammad.pixellab;

import com.alaishat.mohammad.pixellab.domain.image.ImageLoader;
import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFilesStore;
import com.alaishat.mohammad.pixellab.features.imageworkspace.usecase.LoadImageUseCase;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.AddRecentFileUseCase;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.LoadRecentFilesUseCase;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.RemoveRecentFileUseCase;
import com.alaishat.mohammad.pixellab.features.recentfiles.viewmodel.RecentFilesViewModel;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemImageLoader;
import com.alaishat.mohammad.pixellab.infrastructure.persistence.JsonRecentFilesStore;

/**
 * Composition root. Wires together infrastructure, use cases, and view models.
 * Manual DI — reading this class should reveal the entire dependency graph.
 */
public final class AppComponent {

    private final ImageWorkspaceViewModel imageWorkspaceViewModel;
    private final RecentFilesViewModel recentFilesViewModel;

    public AppComponent() {
        ImageLoader imageLoader = new FileSystemImageLoader();
        LoadImageUseCase loadImageUseCase = new LoadImageUseCase(imageLoader);
        this.imageWorkspaceViewModel = new ImageWorkspaceViewModel(loadImageUseCase);

        RecentFilesStore recentFilesStore = new JsonRecentFilesStore();
        this.recentFilesViewModel = new RecentFilesViewModel(
                new LoadRecentFilesUseCase(recentFilesStore),
                new AddRecentFileUseCase(recentFilesStore),
                new RemoveRecentFileUseCase(recentFilesStore));

        // Cross-feature glue: every successful image load gets recorded as a recent.
        // Listening on currentSource keeps ImageWorkspaceViewModel ignorant of recents.
        imageWorkspaceViewModel.currentSourceProperty().addListener((obs, oldPath, newPath) -> {
            if (newPath != null) {
                recentFilesViewModel.recordOpened(newPath);
            }
        });

        recentFilesViewModel.refresh();
    }

    public ImageWorkspaceViewModel imageWorkspaceViewModel() {
        return imageWorkspaceViewModel;
    }

    public RecentFilesViewModel recentFilesViewModel() {
        return recentFilesViewModel;
    }
}
