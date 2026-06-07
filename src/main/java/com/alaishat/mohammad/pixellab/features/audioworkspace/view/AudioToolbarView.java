package com.alaishat.mohammad.pixellab.features.audioworkspace.view;

import com.alaishat.mohammad.pixellab.features.audioworkspace.viewmodel.AudioWorkspaceViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Locale;

/**
 * Top bar for the Audio Lab: Open / Save / Save As / Reset — mirrors {@code
 * features.imageworkspace.view.ToolbarView}. No keyboard accelerators here:
 * the Image Lab toolbar already owns Ctrl+O/S/Shift+S/R on the shared {@code
 * Scene}, and registering the same combinations again would silently steal
 * them regardless of which tab is active.
 */
public final class AudioToolbarView extends ToolBar {

    public AudioToolbarView(AudioWorkspaceViewModel workspaceVm) {
        setPadding(new Insets(4));

        Button openButton = new Button("Open…");
        openButton.setOnAction(e -> openAudio(workspaceVm));

        Button saveButton = new Button("Save");
        saveButton.disableProperty().bind(workspaceVm.hasAudioBinding().not());
        saveButton.setOnAction(e -> workspaceVm.save());

        Button saveAsButton = new Button("Save As…");
        saveAsButton.disableProperty().bind(workspaceVm.hasAudioBinding().not());
        saveAsButton.setOnAction(e -> saveAs(workspaceVm));

        Button resetButton = new Button("Reset");
        resetButton.disableProperty().bind(workspaceVm.hasAudioBinding().not());
        resetButton.setOnAction(e -> workspaceVm.reset());

        getItems().addAll(openButton, saveButton, saveAsButton, resetButton);
    }

    private static void openAudio(AudioWorkspaceViewModel viewModel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open audio file");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio (WAV / AIFF / AU)", "*.wav", "*.wave", "*.aiff", "*.aif", "*.au"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        Window owner = null;
        File picked = chooser.showOpenDialog(owner);
        if (picked != null) {
            viewModel.open(picked.toPath());
        }
    }

    private static void saveAs(AudioWorkspaceViewModel workspaceVm) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save audio as");
        FileChooser.ExtensionFilter wavFilter = new FileChooser.ExtensionFilter("WAV audio", "*.wav");
        FileChooser.ExtensionFilter aiffFilter = new FileChooser.ExtensionFilter("AIFF audio", "*.aiff", "*.aif");
        FileChooser.ExtensionFilter auFilter = new FileChooser.ExtensionFilter("AU audio", "*.au");
        chooser.getExtensionFilters().addAll(wavFilter, aiffFilter, auFilter);
        chooser.setSelectedExtensionFilter(wavFilter);

        File picked = chooser.showSaveDialog(null);
        if (picked == null) return;

        FileChooser.ExtensionFilter selected = chooser.getSelectedExtensionFilter();
        String format;
        String preferredExt;
        if (selected == aiffFilter) {
            format = "AIFF";
            preferredExt = ".aiff";
        } else if (selected == auFilter) {
            format = "AU";
            preferredExt = ".au";
        } else {
            format = "WAV";
            preferredExt = ".wav";
        }

        File target = ensureExtension(picked, preferredExt);
        workspaceVm.saveAs(target.toPath(), format);
    }

    private static File ensureExtension(File picked, String preferredExt) {
        String name = picked.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".wav") || name.endsWith(".aiff") || name.endsWith(".aif") || name.endsWith(".au")) {
            return picked;
        }
        return new File(picked.getParentFile(), picked.getName() + preferredExt);
    }
}
