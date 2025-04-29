package io.github.eisopux.diagnostics.core;

/** A pluggable interface for generating output from collected data. */
public interface Reporter {

    /**
     * Produce a formatted report from the given {@link CompilationReportData}.
     *
     * @param reportData the aggregated report data from collectors.
     */
    void generateReport(CompilationReportData reportData);
}
