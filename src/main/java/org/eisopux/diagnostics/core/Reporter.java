package org.eisopux.diagnostics.core;

import java.util.List;

/** A pluggable interface for generating output from collected data. */
public interface Reporter {

    /**
     * Produce a formatted report given:
     *
     * @param collectors The active collectors after compilation
     */
    void generateReport(List<? extends Collector<?>> collectors);
}
