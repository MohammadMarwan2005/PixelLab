package com.alaishat.mohammad.pixellab.features.colorpicker.viewmodel;

import com.alaishat.mohammad.pixellab.domain.color.Cmyk;
import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbCmyk;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbHsv;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbLab;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbYCbCr;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbYuv;
import com.alaishat.mohammad.pixellab.features.colorpicker.usecase.CopyToClipboardUseCase;
import com.alaishat.mohammad.pixellab.features.visualization3d.usecase.ColorSample;
import com.alaishat.mohammad.pixellab.features.visualization3d.viewmodel.ColorSpaceVisualizationViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.Objects;

/**
 * Drives the synchronization panel (Phase 8.8–8.9, Req 5): exposes string
 * bindings for the picked color in all six color systems, plus per-row copy
 * actions backed by {@link CopyToClipboardUseCase}.
 */
public final class ColorPickerViewModel {

    private final ReadOnlyObjectProperty<ColorSample> source;
    private final CopyToClipboardUseCase copy;

    public ColorPickerViewModel(ColorSpaceVisualizationViewModel visualizationViewModel,
                                CopyToClipboardUseCase copy) {
        Objects.requireNonNull(visualizationViewModel, "visualizationViewModel");
        this.copy = Objects.requireNonNull(copy, "copy");
        ReadOnlyObjectWrapper<ColorSample> wrapper = new ReadOnlyObjectWrapper<>();
        wrapper.bind(visualizationViewModel.pickedSampleProperty());
        this.source = wrapper.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<ColorSample> pickedSampleProperty() {
        return source;
    }

    public StringBinding rgbText() {
        return Bindings.createStringBinding(() -> {
            ColorSample s = source.get();
            if (s == null) return "—";
            ColorTriplet rgb = s.rgb();
            return String.format("R %.0f, G %.0f, B %.0f",
                    rgb.a() * 255, rgb.b() * 255, rgb.c() * 255);
        }, source);
    }

    public StringBinding hsvText() {
        return Bindings.createStringBinding(() -> {
            ColorSample s = source.get();
            if (s == null) return "—";
            ColorTriplet hsv = RgbHsv.toHsv(s.rgb());
            return String.format("H %.0f°, S %.0f%%, V %.0f%%", hsv.a(), hsv.b() * 100, hsv.c() * 100);
        }, source);
    }

    public StringBinding cmykText() {
        return Bindings.createStringBinding(() -> {
            ColorSample s = source.get();
            if (s == null) return "—";
            Cmyk c = RgbCmyk.toCmyk(s.rgb());
            return String.format("C %.0f%%, M %.0f%%, Y %.0f%%, K %.0f%%",
                    c.c() * 100, c.m() * 100, c.y() * 100, c.k() * 100);
        }, source);
    }

    public StringBinding yuvText() {
        return Bindings.createStringBinding(() -> {
            ColorSample s = source.get();
            if (s == null) return "—";
            ColorTriplet yuv = RgbYuv.toYuv(s.rgb());
            return String.format("Y %.2f, U %.2f, V %.2f", yuv.a(), yuv.b(), yuv.c());
        }, source);
    }

    public StringBinding ycbcrText() {
        return Bindings.createStringBinding(() -> {
            ColorSample s = source.get();
            if (s == null) return "—";
            ColorTriplet ycc = RgbYCbCr.toYCbCr(s.rgb());
            return String.format("Y %.2f, Cb %.2f, Cr %.2f", ycc.a(), ycc.b(), ycc.c());
        }, source);
    }

    public StringBinding labText() {
        return Bindings.createStringBinding(() -> {
            ColorSample s = source.get();
            if (s == null) return "—";
            ColorTriplet lab = RgbLab.toLab(s.rgb());
            return String.format("L %.1f, a %.1f, b %.1f", lab.a(), lab.b(), lab.c());
        }, source);
    }

    public void copyText(String text) {
        copy.execute(text);
    }
}
