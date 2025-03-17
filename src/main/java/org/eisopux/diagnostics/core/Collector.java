package org.eisopux.diagnostics.core;

import java.util.List;

public interface Collector<T> {

    /**
     * Called before the javac compilation task is executed. Use this to register any TaskListeners
     * or initialize data structures.
     */
    default void onBeforeCompile(CompilationTaskBuilder builder) {}

    /**
     * Called after compilation. The collector should finalize its data and contribute its section
     * into the given report.
     */
    default void onAfterCompile() {}

    List<T> getItems();
}
