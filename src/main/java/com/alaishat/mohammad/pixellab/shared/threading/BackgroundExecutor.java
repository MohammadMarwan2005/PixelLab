package com.alaishat.mohammad.pixellab.shared.threading;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single-thread, stage-aware "latest wins" task queue (Phase 9.1, Req 6).
 *
 * <p>Tasks are submitted with a {@code key}. Submitting with a key already
 * present in the pending queue replaces the existing task at that slot
 * (keeping its position) — useful for slider drags, where only the most
 * recent value matters. Different keys coexist so stages of the pipeline
 * (channels → quantize → display) don't clobber each other.
 *
 * <p>The {@link #busyProperty()} flips to true while a task is running and to
 * false when the queue drains. Listeners run on the JavaFX Application Thread
 * so they can drive UI directly.
 */
public final class BackgroundExecutor {

    private final Thread worker;
    private final LinkedHashMap<Object, Runnable> pending = new LinkedHashMap<>();
    private final Object lock = new Object();
    private final ReadOnlyBooleanWrapper busy = new ReadOnlyBooleanWrapper(false);

    public BackgroundExecutor() {
        worker = new Thread(this::loop, "pixellab-background");
        worker.setDaemon(true);
        worker.start();
    }

    public void submit(Object key, Runnable task) {
        synchronized (lock) {
            pending.put(key, task);
            lock.notifyAll();
        }
    }

    public ReadOnlyBooleanProperty busyProperty() {
        return busy.getReadOnlyProperty();
    }

    public void shutdown() {
        worker.interrupt();
    }

    private void loop() {
        try {
            while (true) {
                Runnable task;
                synchronized (lock) {
                    while (pending.isEmpty()) lock.wait();
                    Iterator<Map.Entry<Object, Runnable>> it = pending.entrySet().iterator();
                    task = it.next().getValue();
                    it.remove();
                }
                Platform.runLater(() -> busy.set(true));
                try {
                    task.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    boolean drained;
                    synchronized (lock) {
                        drained = pending.isEmpty();
                    }
                    if (drained) Platform.runLater(() -> busy.set(false));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
