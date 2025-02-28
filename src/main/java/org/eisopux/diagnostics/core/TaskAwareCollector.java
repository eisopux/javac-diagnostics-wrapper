package org.eisopux.diagnostics.core;

import javax.tools.JavaCompiler;

/**
 * A specialized collector that needs direct access to the CompilationTask
 * (for example, to add a TaskListener).
 */
public interface TaskAwareCollector extends JavacCollector {
    /**
     * Called immediately after the {@link JavaCompiler.CompilationTask} is created,
     * but before {@code task.call()} is invoked. This allows the collector to
     * register TaskListeners or other javac-specific hooks.
     */
    void registerWithTask(JavaCompiler.CompilationTask task);
}

