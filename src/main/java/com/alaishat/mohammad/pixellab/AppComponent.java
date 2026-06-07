package com.alaishat.mohammad.pixellab;

import com.alaishat.mohammad.pixellab.domain.audio.AudioLoader;
import com.alaishat.mohammad.pixellab.domain.audio.AudioPlayer;
import com.alaishat.mohammad.pixellab.domain.audio.AudioSaver;
import com.alaishat.mohammad.pixellab.domain.audio.compression.AdaptiveDeltaModulationCodec;
import com.alaishat.mohammad.pixellab.domain.audio.compression.AudioCodec;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressedAudioStore;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionAlgorithm;
import com.alaishat.mohammad.pixellab.domain.audio.compression.DeltaModulationCodec;
import com.alaishat.mohammad.pixellab.domain.audio.compression.DpcmCodec;
import com.alaishat.mohammad.pixellab.domain.image.ImageLoader;
import com.alaishat.mohammad.pixellab.domain.image.ImageSaver;
import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFilesStore;
import com.alaishat.mohammad.pixellab.features.audiocompression.usecase.CompressAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audiocompression.usecase.DecompressAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audiocompression.usecase.SaveCompressedAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audiocompression.viewmodel.AudioCompressionViewModel;
import com.alaishat.mohammad.pixellab.features.audioworkspace.usecase.LoadAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audioworkspace.usecase.ResetAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audioworkspace.usecase.SaveAsAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audioworkspace.usecase.SaveAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audioworkspace.viewmodel.AudioWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.channels.usecase.ApplyChannelAdjustmentsUseCase;
import com.alaishat.mohammad.pixellab.features.channels.usecase.SplitChannelsUseCase;
import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel;
import com.alaishat.mohammad.pixellab.features.colorpicker.usecase.CopyToClipboardUseCase;
import com.alaishat.mohammad.pixellab.features.colorpicker.viewmodel.ColorPickerViewModel;
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
import com.alaishat.mohammad.pixellab.features.visualization3d.usecase.SampleColorSpaceUseCase;
import com.alaishat.mohammad.pixellab.features.visualization3d.viewmodel.ColorSpaceVisualizationViewModel;
import com.alaishat.mohammad.pixellab.infrastructure.audio.JavaSoundAudioPlayer;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemAudioLoader;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemAudioSaver;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemCompressedAudioStore;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemImageLoader;
import com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemImageSaver;
import com.alaishat.mohammad.pixellab.infrastructure.persistence.JsonRecentFilesStore;
import com.alaishat.mohammad.pixellab.shared.threading.BackgroundExecutor;
import com.alaishat.mohammad.pixellab.shared.threading.UpdateCoalescer;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Composition root. Wires together infrastructure, use cases, and view models.
 * Manual DI — reading this class should reveal the entire dependency graph.
 */
public final class AppComponent {

    private final BackgroundExecutor backgroundExecutor;
    private final UpdateCoalescer updateCoalescer;

    private final ImageWorkspaceViewModel imageWorkspaceViewModel;
    private final EditSessionViewModel editSessionViewModel;
    private final ColorSpaceViewModel colorSpaceViewModel;
    private final ChannelsViewModel channelsViewModel;
    private final QuantizationViewModel quantizationViewModel;
    private final ColorSpaceVisualizationViewModel visualizationViewModel;
    private final ColorPickerViewModel colorPickerViewModel;
    private final RecentFilesViewModel recentFilesViewModel;
    private final AudioWorkspaceViewModel audioWorkspaceViewModel;
    private final AudioCompressionViewModel audioCompressionViewModel;
    private final ExecutorService compressionExecutor;

    public AppComponent() {
        this.backgroundExecutor = new BackgroundExecutor();
        this.updateCoalescer = new UpdateCoalescer(backgroundExecutor);

        ImageLoader imageLoader = new FileSystemImageLoader();
        ImageSaver imageSaver = new FileSystemImageSaver();

        this.imageWorkspaceViewModel = new ImageWorkspaceViewModel(new LoadImageUseCase(imageLoader));
        this.colorSpaceViewModel = new ColorSpaceViewModel(
                imageWorkspaceViewModel,
                new ConvertColorSpaceUseCase(),
                updateCoalescer);

        // Pipeline: ChannelsViewModel emits channelAdjustedBuffer; QuantizationViewModel
        // consumes it and writes the final working buffer. Both run on the bg executor.
        this.channelsViewModel = new ChannelsViewModel(
                imageWorkspaceViewModel,
                colorSpaceViewModel,
                new ApplyChannelAdjustmentsUseCase(),
                new SplitChannelsUseCase(),
                updateCoalescer);
        this.quantizationViewModel = new QuantizationViewModel(
                imageWorkspaceViewModel,
                channelsViewModel,
                new QuantizeColorsUseCase(),
                updateCoalescer);

        this.visualizationViewModel = new ColorSpaceVisualizationViewModel(
                colorSpaceViewModel,
                new SampleColorSpaceUseCase());
        this.colorPickerViewModel = new ColorPickerViewModel(visualizationViewModel, new CopyToClipboardUseCase());

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

        AudioLoader audioLoader = new FileSystemAudioLoader();
        AudioSaver audioSaver = new FileSystemAudioSaver();
        AudioPlayer audioPlayer = new JavaSoundAudioPlayer();
        this.audioWorkspaceViewModel = new AudioWorkspaceViewModel(
                new LoadAudioUseCase(audioLoader),
                new ResetAudioUseCase(),
                new SaveAudioUseCase(audioSaver),
                new SaveAsAudioUseCase(audioSaver),
                audioPlayer);

        // One codec per algorithm (Req. 4) — CompressAudioUseCase/DecompressAudioUseCase
        // pick the right one by CompressionAlgorithm key, same shape as the
        // image side's "one converter per color space" maps.
        Map<CompressionAlgorithm, AudioCodec> codecs = Map.of(
                CompressionAlgorithm.DELTA_MODULATION, new DeltaModulationCodec(),
                CompressionAlgorithm.ADAPTIVE_DELTA_MODULATION, new AdaptiveDeltaModulationCodec(),
                CompressionAlgorithm.DPCM, new DpcmCodec());
        CompressedAudioStore compressedAudioStore = new FileSystemCompressedAudioStore();

        // Compression jobs are one-shot, cancellable, and must run to completion in
        // order — a dedicated single-thread executor (separate from backgroundExecutor,
        // which is for "latest-wins" live recompute) is the right shape for that.
        this.compressionExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "audio-compression");
            thread.setDaemon(true);
            return thread;
        });
        this.audioCompressionViewModel = new AudioCompressionViewModel(
                audioWorkspaceViewModel,
                new CompressAudioUseCase(codecs),
                new DecompressAudioUseCase(codecs),
                new SaveCompressedAudioUseCase(compressedAudioStore),
                compressionExecutor);
    }

    public void shutdown() {
        backgroundExecutor.shutdown();
        compressionExecutor.shutdown();
    }

    public ImageWorkspaceViewModel imageWorkspaceViewModel()           { return imageWorkspaceViewModel; }
    public EditSessionViewModel editSessionViewModel()                 { return editSessionViewModel; }
    public ColorSpaceViewModel colorSpaceViewModel()                   { return colorSpaceViewModel; }
    public ChannelsViewModel channelsViewModel()                       { return channelsViewModel; }
    public QuantizationViewModel quantizationViewModel()               { return quantizationViewModel; }
    public ColorSpaceVisualizationViewModel visualizationViewModel()   { return visualizationViewModel; }
    public ColorPickerViewModel colorPickerViewModel()                 { return colorPickerViewModel; }
    public RecentFilesViewModel recentFilesViewModel()                 { return recentFilesViewModel; }
    public UpdateCoalescer updateCoalescer()                           { return updateCoalescer; }
    public AudioWorkspaceViewModel audioWorkspaceViewModel()           { return audioWorkspaceViewModel; }
    public AudioCompressionViewModel audioCompressionViewModel()       { return audioCompressionViewModel; }
}
