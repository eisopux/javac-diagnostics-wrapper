package org.eisopux.diagnostics.core;

/**
 * A pluggable interface for collecting information during javac compilation.
 *
 * <p>Implementations may use {@link com.sun.source.util.TaskListener} or other mechanisms to gather
 * diagnostics, annotation processor info, or other metadata.
 */
public interface JavacCollector {

    /**
     * Called before the javac compilation task is executed.
     * Use this to register any TaskListeners or initialize data structures.
     */
    default void onBeforeCompile() {
        // Default no-op
    }

    /**
     * Called after the javac compilation task has finished.
     *
     * @param success {@code true} if the compilation succeeded; otherwise {@code false}.
     */
    default void onAfterCompile(boolean success) {
        // Default no-op
    }
}
