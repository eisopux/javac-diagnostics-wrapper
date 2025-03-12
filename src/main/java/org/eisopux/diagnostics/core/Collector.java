package org.eisopux.diagnostics.core;

import java.util.List;

import javax.tools.JavaCompiler;

public interface Collector<T> {

    /**
     * Called before the javac compilation task is executed. Use this to register any TaskListeners
     * or initialize data structures.
     */
    default void onBeforeCompile() {
        // Default no-op
    }

    default void attachToTask(JavaCompiler.CompilationTask task) {
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

    List<T> getItems();
}
