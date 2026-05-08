package com.alaishat.mohammad.pixellab.features.visualization3d.viewmodel;

import com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel;
import com.alaishat.mohammad.pixellab.features.visualization3d.usecase.ColorSample;
import com.alaishat.mohammad.pixellab.features.visualization3d.usecase.SampleColorSpaceUseCase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

public final class ColorSpaceVisualizationViewModel {

    private final ColorSpaceViewModel colorSpace;
    private final SampleColorSpaceUseCase sampleUseCase;

    private final ObservableList<ColorSample> samples = FXCollections.observableArrayList();
    private final ObservableList<ColorSample> samplesView = FXCollections.unmodifiableObservableList(samples);
    private final ObjectProperty<ColorSample> pickedSample = new SimpleObjectProperty<>();

    public ColorSpaceVisualizationViewModel(ColorSpaceViewModel colorSpace,
                                            SampleColorSpaceUseCase sampleUseCase) {
        this.colorSpace = Objects.requireNonNull(colorSpace, "colorSpace");
        this.sampleUseCase = Objects.requireNonNull(sampleUseCase, "sampleUseCase");

        colorSpace.currentSpaceProperty().addListener((obs, old, neu) -> resample());
        resample();
    }

    public ObservableList<ColorSample> samples() {
        return samplesView;
    }

    public ObjectProperty<ColorSample> pickedSampleProperty() {
        return pickedSample;
    }

    private void resample() {
        samples.setAll(sampleUseCase.execute(colorSpace.currentSpaceProperty().get()));
        // Clear any previous pick — the old sample isn't part of the new space.
        pickedSample.set(null);
    }
}
