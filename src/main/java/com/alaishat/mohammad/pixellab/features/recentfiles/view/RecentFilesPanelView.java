package com.alaishat.mohammad.pixellab.features.recentfiles.view;

import com.alaishat.mohammad.pixellab.domain.recentfiles.RecentFile;
import com.alaishat.mohammad.pixellab.features.recentfiles.viewmodel.RecentFilesViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Left pane: list of recently opened files (Phase 3.4–3.6).
 *
 *  - Single click selects, double click opens.
 *  - If the file no longer exists, we show an alert and remove the entry — the
 *    user gets feedback rather than a silent IO failure.
 */
public final class RecentFilesPanelView extends VBox {

    public RecentFilesPanelView(RecentFilesViewModel viewModel, Consumer<Path> onOpen) {
        setPadding(new Insets(12));
        setPrefWidth(220);
        setMinWidth(160);
        setSpacing(8);

        Label heading = new Label("Recent files");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ListView<RecentFile> listView = new ListView<>(viewModel.recents());
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setPlaceholder(new Label("No recent files."));
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setCellFactory(lv -> new RecentFileCell());

        listView.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY || event.getClickCount() != 2) return;
            RecentFile selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            handleOpen(selected, viewModel, onOpen);
        });

        getChildren().addAll(heading, listView);
    }

    private static void handleOpen(RecentFile file, RecentFilesViewModel viewModel, Consumer<Path> onOpen) {
        if (Files.exists(file.path())) {
            onOpen.accept(file.path());
            return;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("File not found");
        alert.setHeaderText("This file no longer exists");
        alert.setContentText(file.path().toString() + "\n\nIt has been removed from recent files.");
        alert.showAndWait();
        viewModel.removeById(file.id());
    }

    private static final class RecentFileCell extends ListCell<RecentFile> {
        private final VBox content = new VBox();
        private final Label nameLabel = new Label();
        private final Label pathLabel = new Label();

        RecentFileCell() {
            nameLabel.setStyle("-fx-font-weight: bold;");
            pathLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
            content.getChildren().addAll(nameLabel, pathLabel);
        }

        @Override
        protected void updateItem(RecentFile item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            Path p = item.path();
            nameLabel.setText(p.getFileName() == null ? p.toString() : p.getFileName().toString());
            Path parent = p.getParent();
            pathLabel.setText(parent == null ? "" : parent.toString());
            setGraphic(content);
        }
    }
}
