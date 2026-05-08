package com.alaishat.mohammad.pixellab.features.colorspace.viewmodel;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.colorspace.usecase.ConvertColorSpaceUseCase;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Objects;

/**
 * Controls which color space the canvas visualizes. Owns all writes to
 * {@link ImageWorkspaceViewModel#currentBufferProperty()}: when the selected
 * space, the loaded session, or the working buffer changes, this view model
 * recomputes the displayed buffer (RGB → working buffer as-is; non-RGB →
 * channel-encoded conversion via {@link ConvertColorSpaceUseCase}).
 */
public final class ColorSpaceViewModel {

    private final ImageWorkspaceViewModel workspace;
    private final ConvertColorSpaceUseCase convert;

    private final ObjectProperty<ColorSpace> currentSpace = new SimpleObjectProperty<>(ColorSpace.RGB);

    public ColorSpaceViewModel(ImageWorkspaceViewModel workspace, ConvertColorSpaceUseCase convert) {
        this.workspace = Objects.requireNonNull(workspace, "workspace");
        this.convert = Objects.requireNonNull(convert, "convert");

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
        PixelBuffer display = (space == ColorSpace.RGB)
                ? session.workingBuffer()
                : convert.execute(session.workingBuffer(), space);
        workspace.currentBufferProperty().set(display);
    }
}
