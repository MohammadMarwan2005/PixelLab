package com.alaishat.mohammad.pixellab.features.channels.viewmodel;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.channels.usecase.ApplyChannelAdjustmentsUseCase;
import com.alaishat.mohammad.pixellab.features.channels.usecase.ChannelAdjustment;
import com.alaishat.mohammad.pixellab.features.channels.usecase.ChannelCodec;
import com.alaishat.mohammad.pixellab.features.channels.usecase.SplitChannelsUseCase;
import com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds per-channel sliders + toggles + thumbnails for the right panel.
 *
 * <p>Reconstruction model: every change starts from the session's <i>original</i>
 * RGB buffer, applies all current channel adjustments in a single pass, and
 * replaces the working buffer. Sliders therefore don't compound on themselves
 * (drag right then left = no net change), and Reset trivially clears the
 * sliders to put the working buffer back.
 *
 * <p>The channel list is rebuilt whenever the color space or session changes —
 * "channel 0" means different things in RGB vs HSV.
 */
public final class ChannelsViewModel {

    private final ImageWorkspaceViewModel workspace;
    private final ColorSpaceViewModel colorSpace;
    private final ApplyChannelAdjustmentsUseCase applyAdjustments;
    private final SplitChannelsUseCase splitChannels;

    private final ObservableList<ChannelControl> channels = FXCollections.observableArrayList();
    private final ObservableList<ChannelControl> channelsView = FXCollections.unmodifiableObservableList(channels);

    /** Set true while we apply our own changes, so we don't recompute from a listener triggered by ourselves. */
    private boolean applying = false;

    public ChannelsViewModel(ImageWorkspaceViewModel workspace,
                             ColorSpaceViewModel colorSpace,
                             ApplyChannelAdjustmentsUseCase applyAdjustments,
                             SplitChannelsUseCase splitChannels) {
        this.workspace = Objects.requireNonNull(workspace, "workspace");
        this.colorSpace = Objects.requireNonNull(colorSpace, "colorSpace");
        this.applyAdjustments = Objects.requireNonNull(applyAdjustments, "applyAdjustments");
        this.splitChannels = Objects.requireNonNull(splitChannels, "splitChannels");

        colorSpace.currentSpaceProperty().addListener((obs, old, neu) -> rebuildChannels());
        workspace.editSessionProperty().addListener((obs, old, neu) -> rebuildChannels());
        rebuildChannels();
    }

    public ObservableList<ChannelControl> channels() {
        return channelsView;
    }

    public void resetAll() {
        applying = true;
        try {
            for (ChannelControl c : channels) {
                c.offset.set(0);
                c.enabled.set(true);
            }
        } finally {
            applying = false;
        }
        recompute();
    }

    private void rebuildChannels() {
        applying = true;
        try {
            channels.clear();
            ColorSpace space = colorSpace.currentSpaceProperty().get();
            for (int i = 0; i < space.componentCount(); i++) {
                ChannelControl c = new ChannelControl(i, space.componentLabel(i));
                attachListeners(c);
                channels.add(c);
            }
        } finally {
            applying = false;
        }
        recompute();
    }

    private void attachListeners(ChannelControl c) {
        c.offset.addListener((obs, old, neu) -> { if (!applying) recompute(); });
        c.enabled.addListener((obs, old, neu) -> { if (!applying) recompute(); });
    }

    private void recompute() {
        EditSession session = workspace.editSessionProperty().get();
        if (session == null) {
            for (ChannelControl c : channels) {
                c.thumbnail.set(null);
            }
            return;
        }
        ColorSpace space = colorSpace.currentSpaceProperty().get();
        ChannelAdjustment[] adjustments = new ChannelAdjustment[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            ChannelControl c = channels.get(i);
            if (!c.enabled.get()) {
                adjustments[i] = ChannelAdjustment.disabled();
            } else {
                double rawOffset = c.offset.get() * ChannelCodec.naturalRange(space, i);
                adjustments[i] = ChannelAdjustment.offset(rawOffset);
            }
        }

        PixelBuffer reconstructed = applyAdjustments.execute(session.originalBuffer(), space, adjustments);
        session.replaceWorking(reconstructed);
        workspace.republishWorkingBuffer();

        // Thumbnails are computed off the working buffer so they show the current adjusted state.
        List<PixelBuffer> grays = splitChannels.execute(reconstructed, space);
        for (int i = 0; i < channels.size(); i++) {
            channels.get(i).thumbnail.set(grays.get(i));
        }
    }

    /** Per-channel UI state: index, label, offset (-1..+1 of natural range), enabled flag, thumbnail buffer. */
    public static final class ChannelControl {
        private final int index;
        private final String label;
        private final DoubleProperty offset = new SimpleDoubleProperty(0.0);
        private final BooleanProperty enabled = new SimpleBooleanProperty(true);
        private final ObjectProperty<PixelBuffer> thumbnail = new SimpleObjectProperty<>();

        ChannelControl(int index, String label) {
            this.index = index;
            this.label = label;
        }

        public int index() { return index; }
        public String label() { return label; }
        public DoubleProperty offsetProperty() { return offset; }
        public BooleanProperty enabledProperty() { return enabled; }
        public ObjectProperty<PixelBuffer> thumbnailProperty() { return thumbnail; }
    }
}
