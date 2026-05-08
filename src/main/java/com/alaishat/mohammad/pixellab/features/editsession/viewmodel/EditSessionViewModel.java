package com.alaishat.mohammad.pixellab.features.editsession.viewmodel;

import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.features.editsession.usecase.ResetUseCase;
import com.alaishat.mohammad.pixellab.features.editsession.usecase.SaveAsImageUseCase;
import com.alaishat.mohammad.pixellab.features.editsession.usecase.SaveImageUseCase;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class EditSessionViewModel {

    private final ImageWorkspaceViewModel workspace;
    private final ResetUseCase resetUseCase;
    private final SaveImageUseCase saveUseCase;
    private final SaveAsImageUseCase saveAsUseCase;

    private final ReadOnlyObjectWrapper<Throwable> lastError = new ReadOnlyObjectWrapper<>();

    public EditSessionViewModel(ImageWorkspaceViewModel workspace,
                                ResetUseCase resetUseCase,
                                SaveImageUseCase saveUseCase,
                                SaveAsImageUseCase saveAsUseCase) {
        this.workspace = Objects.requireNonNull(workspace, "workspace");
        this.resetUseCase = Objects.requireNonNull(resetUseCase, "resetUseCase");
        this.saveUseCase = Objects.requireNonNull(saveUseCase, "saveUseCase");
        this.saveAsUseCase = Objects.requireNonNull(saveAsUseCase, "saveAsUseCase");
    }

    public BooleanBinding canEditBinding() {
        return workspace.editSessionProperty().isNotNull();
    }

    public ReadOnlyObjectProperty<Throwable> lastErrorProperty() {
        return lastError.getReadOnlyProperty();
    }

    public void reset() {
        EditSession session = workspace.editSessionProperty().get();
        if (session == null) return;
        try {
            resetUseCase.execute(session);
            workspace.republishWorkingBuffer();
            lastError.set(null);
        } catch (RuntimeException e) {
            lastError.set(e);
        }
    }

    public void save() {
        EditSession session = workspace.editSessionProperty().get();
        if (session == null) return;
        try {
            saveUseCase.execute(session);
            lastError.set(null);
        } catch (IOException | RuntimeException e) {
            lastError.set(e);
        }
    }

    public void saveAs(Path target, String format) {
        EditSession session = workspace.editSessionProperty().get();
        if (session == null) return;
        try {
            saveAsUseCase.execute(session, target, format);
            lastError.set(null);
        } catch (IOException | RuntimeException e) {
            lastError.set(e);
        }
    }
}
