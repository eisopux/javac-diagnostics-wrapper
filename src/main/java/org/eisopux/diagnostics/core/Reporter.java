package org.eisopux.diagnostics.core;

/** A pluggable interface for generating output from collected data. */
public interface Reporter {

    /** Produce a formatted report given: */
    void generateReport(CompilationReportData reportData);
}
