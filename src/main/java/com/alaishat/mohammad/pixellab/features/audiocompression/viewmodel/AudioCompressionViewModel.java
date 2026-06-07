package com.alaishat.mohammad.pixellab.features.audiocompression.viewmodel;

import com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer;
import com.alaishat.mohammad.pixellab.domain.audio.AudioEditSession;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionAlgorithm;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionProgressListener;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionReport;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionSettings;
import com.alaishat.mohammad.pixellab.domain.audio.compression.EncodedAudio;
import com.alaishat.mohammad.pixellab.features.audiocompression.usecase.CompressAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audiocompression.usecase.DecompressAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audiocompression.usecase.SaveCompressedAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audioworkspace.viewmodel.AudioWorkspaceViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Drives a compression run as a cancellable, progress-reporting background job
 * (Reqs. 6, 7, 8) and exposes its live and final results — encoded bitstream,
 * report, and two live chart series — to the view. Also drives the
 * decompress round trip (Req. 5), installing the result as the workspace's
 * working buffer.
 *
 * <p><b>Why {@code javafx.concurrent.Task} instead of {@code BackgroundExecutor}:</b>
 * see the plan's "Threading" decision — {@code BackgroundExecutor}/{@code
 * UpdateCoalescer} implement "latest-wins, replace the pending job", which is
 * right for live recompute but wrong for a one-shot job that must run to
 * completion, report fine-grained progress, and be explicitly cancellable.
 * {@code Task} ships exactly that shape for free.
 *
 * <p><b>Why the "compression ratio" chart looks flat:</b> all three algorithms
 * are *fixed-rate* codes (DM/ADM always emit 1 bit/sample, DPCM always emits
 * {@code quantizationBits} bits/sample — no entropy coding). So the ratio
 * {@code originalBytesSoFar / compressedBytesSoFar} converges to the constant
 * {@code bitDepth / bitsPerSample} almost immediately; the chart honestly shows
 * that convergence rather than fabricating drift that wouldn't reflect how
 * these algorithms actually behave — itself a useful talking point for a
 * college demo ("why doesn't the ratio change over time?").
 */
public final class AudioCompressionViewModel {

    private final AudioWorkspaceViewModel workspace;
    private final CompressAudioUseCase compressAudio;
    private final DecompressAudioUseCase decompressAudio;
    private final SaveCompressedAudioUseCase saveCompressedAudio;
    private final ExecutorService compressionExecutor;

    private final ObjectProperty<CompressionAlgorithm> algorithm =
            new SimpleObjectProperty<>(CompressionAlgorithm.DELTA_MODULATION);
    private final IntegerProperty quantizationBits =
            new SimpleIntegerProperty(CompressionSettings.defaults().quantizationBits());
    private final DoubleProperty stepSize =
            new SimpleDoubleProperty(CompressionSettings.defaults().stepSize());
    private final DoubleProperty adaptationFactor =
            new SimpleDoubleProperty(CompressionSettings.defaults().adaptationFactor());

    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(0);
    private final ReadOnlyObjectWrapper<EncodedAudio> encodedAudio = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<CompressionReport> report = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Throwable> lastError = new ReadOnlyObjectWrapper<>();

    private final ObservableList<XYChart.Data<Number, Number>> ratioSeries = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data<Number, Number>> speedSeries = FXCollections.observableArrayList();

    private CompressionTask activeTask;

    /**
     * {@code republishWorkingBuffer()} fires for two reasons: Reset (the
     * compressed result describes audio that no longer matches the working
     * buffer — must clear) and our own {@link #decompress()} (the result is
     * exactly what produced the new working buffer — must NOT clear itself).
     * This flag lets the revision listener tell those two apart without the
     * workspace view model needing to know compression exists.
     */
    private boolean ignoreNextWorkingBufferRevision;

    public AudioCompressionViewModel(AudioWorkspaceViewModel workspace,
                                      CompressAudioUseCase compressAudio,
                                      DecompressAudioUseCase decompressAudio,
                                      SaveCompressedAudioUseCase saveCompressedAudio,
                                      ExecutorService compressionExecutor) {
        this.workspace = Objects.requireNonNull(workspace, "workspace");
        this.compressAudio = Objects.requireNonNull(compressAudio, "compressAudio");
        this.decompressAudio = Objects.requireNonNull(decompressAudio, "decompressAudio");
        this.saveCompressedAudio = Objects.requireNonNull(saveCompressedAudio, "saveCompressedAudio");
        this.compressionExecutor = Objects.requireNonNull(compressionExecutor, "compressionExecutor");

        // A freshly opened session invalidates any compressed result from the previous one.
        workspace.editSessionProperty().addListener((obs, was, now) -> clearResults());

        // Reset replaces the working buffer in place (same session, same revision
        // property bumped) rather than swapping the session — listen here too,
        // and use the flag above to ignore the bump our own decompress() causes.
        workspace.workingBufferRevisionProperty().addListener((obs, was, now) -> {
            if (ignoreNextWorkingBufferRevision) {
                ignoreNextWorkingBufferRevision = false;
            } else {
                clearResults();
            }
        });
    }

    // --- settings (Req. 6) ---

    public ObjectProperty<CompressionAlgorithm> algorithmProperty()    { return algorithm; }
    public IntegerProperty quantizationBitsProperty()                  { return quantizationBits; }
    public DoubleProperty stepSizeProperty()                           { return stepSize; }
    public DoubleProperty adaptationFactorProperty()                   { return adaptationFactor; }

    // --- run state (Reqs. 7, 8) ---

    public ReadOnlyBooleanProperty runningProperty()                   { return running.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty progressProperty()                   { return progress.getReadOnlyProperty(); }
    public ReadOnlyObjectProperty<Throwable> lastErrorProperty()       { return lastError.getReadOnlyProperty(); }

    // --- results (Reqs. 5, 10, 11) ---

    public ReadOnlyObjectProperty<EncodedAudio> encodedAudioProperty() { return encodedAudio.getReadOnlyProperty(); }
    public ReadOnlyObjectProperty<CompressionReport> reportProperty()  { return report.getReadOnlyProperty(); }

    /** Live "compression ratio so far" samples, x = elapsed seconds, y = ratio. */
    public ObservableList<XYChart.Data<Number, Number>> ratioSeriesData() { return ratioSeries; }

    /** Live "processing speed so far" samples, x = elapsed seconds, y = samples/second. */
    public ObservableList<XYChart.Data<Number, Number>> speedSeriesData() { return speedSeries; }

    public BooleanBinding canCompressBinding() {
        return workspace.hasAudioBinding().and(running.not());
    }

    public BooleanBinding canDecompressBinding() {
        return Bindings.isNotNull(encodedAudio).and(running.not());
    }

    public BooleanBinding canSaveCompressedBinding() {
        return Bindings.isNotNull(encodedAudio).and(running.not());
    }

    // --- actions ---

    /** Reverts the settings sliders to {@link CompressionSettings#defaults()} (Req. 9). */
    public void resetSettings() {
        CompressionSettings defaults = CompressionSettings.defaults();
        quantizationBits.set(defaults.quantizationBits());
        stepSize.set(defaults.stepSize());
        adaptationFactor.set(defaults.adaptationFactor());
    }

    public void compress() {
        if (running.get()) return;
        AudioEditSession session = workspace.editSessionProperty().get();
        if (session == null) return;

        CompressionSettings settings;
        try {
            settings = currentSettings();
        } catch (IllegalArgumentException e) {
            lastError.set(e);
            return;
        }

        clearResults();
        running.set(true);
        progress.set(0);

        CompressionTask task = new CompressionTask(session.workingBuffer().copy(), algorithm.get(), settings);
        activeTask = task;
        progress.bind(task.progressProperty());
        task.setOnSucceeded(e -> onCompressionFinished(task, task.getValue(), null, false));
        task.setOnCancelled(e -> onCompressionFinished(task, null, null, true));
        task.setOnFailed(e -> onCompressionFinished(task, null, task.getException(), false));

        compressionExecutor.submit(task);
    }

    /** Requests cancellation of the in-flight compression run (Req. 8). */
    public void cancel() {
        if (activeTask != null) {
            activeTask.cancel();
        }
    }

    /** Decompresses the most recent result back into the working buffer (Req. 5). */
    public void decompress() {
        if (running.get()) return;
        EncodedAudio encoded = encodedAudio.get();
        AudioEditSession session = workspace.editSessionProperty().get();
        if (encoded == null || session == null) return;

        try {
            AudioBuffer decoded = decompressAudio.execute(encoded, CompressionProgressListener.NONE);
            session.replaceWorking(decoded);
            ignoreNextWorkingBufferRevision = true;
            workspace.republishWorkingBuffer();
            lastError.set(null);
        } catch (RuntimeException e) {
            lastError.set(e);
        }
    }

    /** Writes the most recent compression result to a "PXAC" container (Req. 11). */
    public void saveCompressed(Path target) {
        EncodedAudio encoded = encodedAudio.get();
        if (encoded == null) return;

        try {
            saveCompressedAudio.execute(encoded, target);
            lastError.set(null);
        } catch (IOException | RuntimeException e) {
            lastError.set(e);
        }
    }

    private CompressionSettings currentSettings() {
        return new CompressionSettings(quantizationBits.get(), stepSize.get(), adaptationFactor.get());
    }

    private void onCompressionFinished(CompressionTask task, EncodedAudio result, Throwable error, boolean cancelled) {
        progress.unbind();
        progress.set(cancelled || error != null ? 0 : 1);
        running.set(false);
        activeTask = null;

        if (error != null) {
            lastError.set(error);
        } else if (!cancelled && result != null) {
            encodedAudio.set(result);
            report.set(new CompressionReport(
                    task.originalBytes, result.totalEncodedBytes(), task.elapsedMillis(),
                    result.algorithm(), result.settings()));
            lastError.set(null);
        }
    }

    private void clearResults() {
        encodedAudio.set(null);
        report.set(null);
        ratioSeries.clear();
        speedSeries.clear();
    }

    private static int bitsPerSample(CompressionAlgorithm algorithm, CompressionSettings settings) {
        return switch (algorithm) {
            case DELTA_MODULATION, ADAPTIVE_DELTA_MODULATION -> 1;
            case DPCM -> settings.quantizationBits();
        };
    }

    /**
     * Runs {@link CompressAudioUseCase} off the FX thread, bridging codec
     * progress callbacks to {@link Task#updateProgress} and to the two live
     * chart series (Req. 7) via {@code Platform.runLater}.
     */
    private final class CompressionTask extends Task<EncodedAudio> {

        private final AudioBuffer source;
        private final CompressionAlgorithm chosenAlgorithm;
        private final CompressionSettings chosenSettings;
        private final long originalBytes;
        private final long startNanos = System.nanoTime();
        private volatile long elapsedMillis;

        CompressionTask(AudioBuffer source, CompressionAlgorithm chosenAlgorithm, CompressionSettings chosenSettings) {
            this.source = source;
            this.chosenAlgorithm = chosenAlgorithm;
            this.chosenSettings = chosenSettings;
            this.originalBytes = (long) source.frameCount() * source.channelCount() * (source.bitDepth() / 8);
        }

        long originalBytes()   { return originalBytes; }
        long elapsedMillis()   { return elapsedMillis; }

        @Override
        protected EncodedAudio call() {
            CompressionProgressListener listener = new CompressionProgressListener() {
                @Override
                public void onProgress(long processed, long total) {
                    updateProgress(processed, total);
                    publishLiveMetrics(processed);
                }

                @Override
                public boolean isCancelled() {
                    return CompressionTask.this.isCancelled();
                }
            };
            try {
                return compressAudio.execute(source, chosenAlgorithm, chosenSettings, listener);
            } finally {
                elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            }
        }

        private void publishLiveMetrics(long processed) {
            double elapsedSeconds = (System.nanoTime() - startNanos) / 1_000_000_000.0;
            if (elapsedSeconds <= 0 || processed <= 0) return;

            double samplesPerSecond = processed / elapsedSeconds;
            int bits = bitsPerSample(chosenAlgorithm, chosenSettings);
            long compressedBytesSoFar = (processed * bits + 7) / 8;
            long originalBytesSoFar = processed * (source.bitDepth() / 8);
            double ratio = compressedBytesSoFar == 0 ? 0 : originalBytesSoFar / (double) compressedBytesSoFar;

            Platform.runLater(() -> {
                speedSeries.add(new XYChart.Data<>(elapsedSeconds, samplesPerSecond));
                ratioSeries.add(new XYChart.Data<>(elapsedSeconds, ratio));
            });
        }
    }
}
