package org.eisopux.diagnostics.core;

/**
 * A Collector is responsible for gathering diagnostic or other compiler-related data during a
 * compilation run.
 *
 * <p>Implementations of this interface may be used to attach listeners, perform analysis, or
 * collect additional metadata during the compilation process. The collected data is then
 * incorporated into a {@link CompilationReportData} instance as a list of key/value pairs.
 */
public interface Collector {

    /**
     * Called before the javac compilation task is executed. Use this method to attach the Collector
     * to a {@link CompilationTaskBuilder} or to initialize Collector-specific data structures.
     *
     * @param builder the CompilationTaskBuilder used to create the compilation task
     */
    void onBeforeCompile(CompilationTaskBuilder builder);

    /**
     * Called after compilation. The Collector should finalize its data and populate its section in
     * a {@link CompilationReportData} instance as a list of key/value pairs.
     *
     * @param reportData the CompilationReportData object into which the Collector should insert its
     *     data
     */
    void onAfterCompile(CompilationReportData reportData);
}
