package com.alaishat.mohammad.pixellab.shared.threading;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * "Drop intermediate slider events" helper (Phase 9.2, Req 6).
 *
 * <p>Wraps {@link BackgroundExecutor} with the typical compute-on-background,
 * publish-on-UI dance: caller hands in a {@code compute} supplier (heavy work
 * that may not touch JavaFX state) and a {@code publish} consumer (UI update
 * for the result). The supplier runs on the bg thread; the consumer runs on
 * the JavaFX Application Thread when it finishes.
 */
public final class UpdateCoalescer {

    private final BackgroundExecutor executor;

    public UpdateCoalescer(BackgroundExecutor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public <T> void submit(Object key, Supplier<T> compute, Consumer<T> publish) {
        executor.submit(key, () -> {
            T result;
            try {
                result = compute.get();
            } catch (Throwable t) {
                t.printStackTrace();
                return;
            }
            Platform.runLater(() -> publish.accept(result));
        });
    }

    public ReadOnlyBooleanProperty busyProperty() {
        return executor.busyProperty();
    }
}
