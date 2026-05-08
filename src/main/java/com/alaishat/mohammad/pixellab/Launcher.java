package com.alaishat.mohammad.pixellab;

/**
 * Plain entry point for IDE / classpath launches.
 * Workaround for: "Error: JavaFX runtime components are missing..." which the JVM throws
 * only when the main class itself extends javafx.application.Application and JavaFX is on
 * the classpath. By delegating from a non-Application class, we sidestep that check —
 * `mvn javafx:run` already handles the module path on its own.
 */
public final class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
