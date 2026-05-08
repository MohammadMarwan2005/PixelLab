package com.alaishat.mohammad.pixellab.features.quantization.viewmodel;

import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.quantization.usecase.QuantizeColorsUseCase;
import com.alaishat.mohammad.pixellab.shared.threading.UpdateCoalescer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Objects;

/**
 * Final stage of the processing pipeline. Reads {@link
 * ChannelsViewModel#channelAdjustedBufferProperty()} as input, applies median-cut
 * quantization (or passes through when N = 256), and publishes the result as
 * the session's working buffer. Bumps the workspace revision so the canvas
 * (via {@link com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel})
 * re-renders.
 *
 * <p>Heavy work runs on the shared {@link UpdateCoalescer} (Phase 9) under
 * the {@code "quantize"} key; rapid slider drags are coalesced.
 */
public final class QuantizationViewModel {

    public static final int DEFAULT_COLORS = QuantizeColorsUseCase.MAX_COLORS;
    private static final Object COALESCER_KEY = "quantize:recompute";

    private final ImageWorkspaceViewModel workspace;
    private final ChannelsViewModel channelsViewModel;
    private final QuantizeColorsUseCase quantize;
    private final UpdateCoalescer coalescer;

    private final IntegerProperty colorCount = new SimpleIntegerProperty(DEFAULT_COLORS);

    public QuantizationViewModel(ImageWorkspaceViewModel workspace,
                                 ChannelsViewModel channelsViewModel,
                                 QuantizeColorsUseCase quantize,
                                 UpdateCoalescer coalescer) {
        this.workspace = Objects.requireNonNull(workspace, "workspace");
        this.channelsViewModel = Objects.requireNonNull(channelsViewModel, "channelsViewModel");
        this.quantize = Objects.requireNonNull(quantize, "quantize");
        this.coalescer = Objects.requireNonNull(coalescer, "coalescer");

        colorCount.addListener((obs, old, neu) -> recompute());
        channelsViewModel.channelAdjustedBufferProperty().addListener((obs, old, neu) -> recompute());
    }

    public IntegerProperty colorCountProperty() {
        return colorCount;
    }

    public void resetAll() {
        colorCount.set(DEFAULT_COLORS);
    }

    private void recompute() {
        EditSession session = workspace.editSessionProperty().get();
        PixelBuffer source = channelsViewModel.channelAdjustedBufferProperty().get();
        if (session == null || source == null) {
            return;
        }
        int n = colorCount.get();

        coalescer.submit(COALESCER_KEY,
                () -> (n >= QuantizeColorsUseCase.MAX_COLORS) ? source : quantize.execute(source, n),
                result -> {
                    // Drop stale results — if a different session is loaded or upstream
                    // recomputed while we were running, skip the publish.
                    if (workspace.editSessionProperty().get() != session
                            || channelsViewModel.channelAdjustedBufferProperty().get() != source) {
                        return;
                    }
                    session.replaceWorking(result);
                    workspace.republishWorkingBuffer();
                });
    }
}
