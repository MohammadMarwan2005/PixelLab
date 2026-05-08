package com.alaishat.mohammad.pixellab.features.colorpicker.usecase;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Puts a string on the system clipboard.
 *
 * <p>Note: this use case touches the JavaFX clipboard directly, breaking the
 * usual "no JavaFX in use cases" rule. Clipboard access is inherently a
 * UI/OS concern and a domain port would buy nothing here — kept simple per
 * Phase 8 feedback.
 */
public final class CopyToClipboardUseCase {

    public void execute(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text == null ? "" : text);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
