package com.alaishat.mohammad.pixellab.features.colorspace.view;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Color space selector for the left panel (Phase 5.2). The dropdown is bound
 * directly to the view model's {@code currentSpace} property.
 */
public final class ColorSpaceSelectorView extends VBox {

    public ColorSpaceSelectorView(ColorSpaceViewModel viewModel) {
        setSpacing(6);
        setPadding(new Insets(0, 0, 8, 0));

        Label heading = new Label("Color space");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ComboBox<ColorSpace> combo = new ComboBox<>(FXCollections.observableArrayList(ColorSpace.values()));
        combo.valueProperty().bindBidirectional(viewModel.currentSpaceProperty());
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(ColorSpace space) {
                return space == null ? "" : space.displayName();
            }
            @Override public ColorSpace fromString(String s) { return null; /* read-only display */ }
        });
        combo.setCellFactory(cb -> displayNameCell());
        combo.setButtonCell(displayNameCell());

        getChildren().addAll(heading, combo);
    }

    private static ListCell<ColorSpace> displayNameCell() {
        return new ListCell<>() {
            @Override protected void updateItem(ColorSpace item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName());
            }
        };
    }
}
