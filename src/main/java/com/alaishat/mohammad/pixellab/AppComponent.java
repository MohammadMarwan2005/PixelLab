package com.alaishat.mohammad.pixellab;

import com.alaishat.mohammad.pixellab.domain.image.ImageLoader;
import com.alaishat.mohammad.pixellab.domain.image.ImageSaver;
import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFilesStore;
import com.alaishat.mohammad.pixellab.features.channels.usecase.ApplyChannelAdjustmentsUseCase;
import com.alaishat.mohammad.pixellab.features.channels.usecase.SplitChannelsUseCase;
import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel;
import com.alaishat.mohammad.pixellab.features.colorspace.usecase.ConvertColorSpaceUseCase;
import com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel;
import com.alaishat.mohammad.pixellab.features.editsession.usecase.ResetUseCase;
import com.alaishat.mohammad.pixellab.features.editsession.usecase.SaveAsImageUseCase;
import com.alaishat.mohammad.pixellab.features.editsession.usecase.SaveImageUseCase;
import com.alaishat.mohammad.pixellab.features.editsession.viewmodel.EditSessionViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.usecase.LoadImageUseCase;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.quantization.usecase.QuantizeColorsUseCase;
import com.alaishat.mohammad.pixellab.features.quantization.viewmodel.QuantizationViewModel;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.AddRecentFileUseCase;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.LoadRecentFilesUseCase;
import com.alaishat.mohammad.pixellab.features.recentfiles.usecase.RemoveRecentFileUseCase;
import com.alaishat.mohammad.pixellab.features.recentfiles.viewmodel.RecentFilesViewModel;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemImageLoader;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemImageSaver;
import com.alaishat.mohammad.pixellab.infrastructure.persistence.JsonRecentFilesStore;

/**
 * Composition root. Wires together infrastructure, use cases, and view models.
 * Manual DI — reading this class should reveal the entire dependency graph.
 */
public final class AppComponent {

    private final ImageWorkspaceViewModel imageWorkspaceViewModel;
    private final EditSessionViewModel editSessionViewModel;
    private final ColorSpaceViewModel colorSpaceViewModel;
    private final ChannelsViewModel channelsViewModel;
    private final QuantizationViewModel quantizationViewModel;
    private final RecentFilesViewModel recentFilesViewModel;

    public AppComponent() {
        ImageLoader imageLoader = new FileSystemImageLoader();
        ImageSaver imageSaver = new FileSystemImageSaver();

        this.imageWorkspaceViewModel = new ImageWorkspaceViewModel(new LoadImageUseCase(imageLoader));
        this.colorSpaceViewModel = new ColorSpaceViewModel(
                imageWorkspaceViewModel,
                new ConvertColorSpaceUseCase());

        // Pipeline: ChannelsViewModel emits channelAdjustedBuffer; QuantizationViewModel
        // consumes it and writes the final working buffer.
        this.channelsViewModel = new ChannelsViewModel(
                imageWorkspaceViewModel,
                colorSpaceViewModel,
                new ApplyChannelAdjustmentsUseCase(),
                new SplitChannelsUseCase());
        this.quantizationViewModel = new QuantizationViewModel(
                imageWorkspaceViewModel,
                channelsViewModel,
                new QuantizeColorsUseCase());

        // EditSessionViewModel last — reset() needs to clear channel + quantization state.
        this.editSessionViewModel = new EditSessionViewModel(
                imageWorkspaceViewModel,
                channelsViewModel,
                quantizationViewModel,
                new ResetUseCase(),
                new SaveImageUseCase(imageSaver),
                new SaveAsImageUseCase(imageSaver));

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

    public ImageWorkspaceViewModel imageWorkspaceViewModel() { return imageWorkspaceViewModel; }
    public EditSessionViewModel editSessionViewModel()       { return editSessionViewModel; }
    public ColorSpaceViewModel colorSpaceViewModel()         { return colorSpaceViewModel; }
    public ChannelsViewModel channelsViewModel()             { return channelsViewModel; }
    public QuantizationViewModel quantizationViewModel()     { return quantizationViewModel; }
    public RecentFilesViewModel recentFilesViewModel()       { return recentFilesViewModel; }
}
