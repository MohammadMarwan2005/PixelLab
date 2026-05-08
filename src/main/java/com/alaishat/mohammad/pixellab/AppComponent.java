package com.alaishat.mohammad.pixellab;

import com.alaishat.mohammad.pixellab.domain.image.ImageLoader;
import com.alaishat.mohammad.pixellab.features.imageworkspace.usecase.LoadImageUseCase;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemImageLoader;

/**
 * Composition root. Wires together infrastructure, use cases, and view models.
 * Manual DI — reading this class should reveal the entire dependency graph.
 */
public final class AppComponent {

    private final ImageWorkspaceViewModel imageWorkspaceViewModel;

    public AppComponent() {
        ImageLoader imageLoader = new FileSystemImageLoader();
        LoadImageUseCase loadImageUseCase = new LoadImageUseCase(imageLoader);
        this.imageWorkspaceViewModel = new ImageWorkspaceViewModel(loadImageUseCase);
    }

    public ImageWorkspaceViewModel imageWorkspaceViewModel() {
        return imageWorkspaceViewModel;
    }
}
