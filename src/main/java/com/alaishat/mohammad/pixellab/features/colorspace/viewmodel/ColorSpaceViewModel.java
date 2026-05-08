package com.alaishat.mohammad.pixellab.features.colorspace.viewmodel;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.colorspace.usecase.ConvertColorSpaceUseCase;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.shared.threading.UpdateCoalescer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Objects;

/**
 * Controls which color space the canvas visualizes. Owns all writes to
 * {@link ImageWorkspaceViewModel#currentBufferProperty()}: when the selected
 * space, the loaded session, or the working buffer changes, this view model
 * recomputes the displayed buffer (RGB → working buffer as-is; non-RGB →
 * channel-encoded conversion via {@link ConvertColorSpaceUseCase}).
 *
 * <p>Conversion runs on the shared {@link UpdateCoalescer} (Phase 9) under
 * the {@code "display"} key.
 */
public final class ColorSpaceViewModel {

    private static final Object COALESCER_KEY = "colorspace:display";

    private final ImageWorkspaceViewModel workspace;
    private final ConvertColorSpaceUseCase convert;
    private final UpdateCoalescer coalescer;

    private final ObjectProperty<ColorSpace> currentSpace = new SimpleObjectProperty<>(ColorSpace.RGB);

    public ColorSpaceViewModel(ImageWorkspaceViewModel workspace,
                               ConvertColorSpaceUseCase convert,
                               UpdateCoalescer coalescer) {
        this.workspace = Objects.requireNonNull(workspace, "workspace");
        this.convert = Objects.requireNonNull(convert, "convert");
        this.coalescer = Objects.requireNonNull(coalescer, "coalescer");

        currentSpace.addListener((obs, oldVal, newVal) -> recomputeDisplay());
        workspace.editSessionProperty().addListener((obs, oldVal, newVal) -> recomputeDisplay());
        workspace.workingBufferRevisionProperty().addListener((obs, oldVal, newVal) -> recomputeDisplay());
    }

    public ObjectProperty<ColorSpace> currentSpaceProperty() {
        return currentSpace;
    }

    private void recomputeDisplay() {
        EditSession session = workspace.editSessionProperty().get();
        if (session == null) {
            workspace.currentBufferProperty().set(null);
            return;
        }
        ColorSpace space = currentSpace.get();
        PixelBuffer working = session.workingBuffer();

        if (space == ColorSpace.RGB) {
            // No conversion to schedule — set immediately so the canvas updates
            // on this same frame and we skip the bg hop.
            workspace.currentBufferProperty().set(working);
            return;
        }

        coalescer.submit(COALESCER_KEY,
                () -> convert.execute(working, space),
                display -> {
                    if (workspace.editSessionProperty().get() != session
                            || currentSpace.get() != space
                            || session.workingBuffer() != working) {
                        return;
                    }
                    workspace.currentBufferProperty().set(display);
                });
    }
}
