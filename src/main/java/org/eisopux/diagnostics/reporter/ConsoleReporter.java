package org.eisopux.diagnostics.reporter;

import org.eisopux.diagnostics.core.ConsoleRenderable;
import org.eisopux.diagnostics.core.FormatReporter;
import org.eisopux.diagnostics.core.JavacCollector;

import java.util.List;

/**
 * A very simple reporter that outputs compile success/failure
 * and then delegates console rendering to any collector
 * that implements {@link ConsoleRenderable}.
 */
public class ConsoleReporter implements FormatReporter {

    @Override
    public String generateReport(List<JavacCollector> collectors, boolean compileSuccess) {
        StringBuilder sb = new StringBuilder();

        sb.append("Compilation ")
                .append(compileSuccess ? "Succeeded" : "Failed")
                .append("\n");

        for (JavacCollector collector : collectors) {
            if (collector instanceof ConsoleRenderable) {
                sb.append(((ConsoleRenderable) collector).toConsoleString())
                        .append("\n");
            }
        }
        return sb.toString();
    }
}
