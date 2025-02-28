package org.eisopux.diagnostics.core;

import java.util.List;

/**
 * A pluggable interface for generating output from collected data.
 */
public interface FormatReporter {

    /**
     * Produce a report (as a String) given:
     *
     * @param collectors The active collectors after compilation
     * @param compileSuccess True if compilation succeeded, false otherwise
     * @return A formatted report to be printed or saved
     */
    String generateReport(List<JavacCollector> collectors, boolean compileSuccess);
}
