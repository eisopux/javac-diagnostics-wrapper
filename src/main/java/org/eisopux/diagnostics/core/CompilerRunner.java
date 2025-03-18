package org.eisopux.diagnostics.core;

import java.util.ArrayList;
import java.util.List;

import javax.tools.*;

/**
 * CompilerRunner sets up and executes a Java compilation task using the system Java compiler, while
 * integrating custom data {@link org.eisopux.diagnostics.core.Collector}(s) and a {@link
 * org.eisopux.diagnostics.core.Reporter} to process and format the collected output.
 */
public class CompilerRunner {

    private final List<Collector> collectors = new ArrayList<>();
    private Reporter reporter;

    /**
     * Adds a Collector to the compilation process.
     *
     * @param collector the Collector to add
     * @return this CompilerRunner instance for method chaining
     */
    public CompilerRunner addCollector(Collector collector) {
        this.collectors.add(collector);
        return this;
    }

    /**
     * Sets the Reporter that will generate the final output report.
     *
     * @param reporter the Reporter to set
     * @return this CompilerRunner instance for method chaining
     */
    public CompilerRunner setReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }

    /**
     * Executes the compilation task by:
     *
     * <ul>
     *   <li>Building a CompilationTaskBuilder from the provided command-line arguments
     *   <li>Calling {@link Collector#onBeforeCompile(CompilationTaskBuilder)} on each collector
     *   <li>Building and executing the compilation task
     *   <li>Creating a CompilationReportData instance and allowing each collector to finalize its
     *       data via {@link Collector#onAfterCompile(CompilationReportData)}
     *   <li>Passing the aggregated report data to the Reporter to generate a formatted output
     * </ul>
     *
     * @param args the command-line arguments to be used in the compilation task
     */
    public void run(String[] args) {

        CompilationTaskBuilder builder = CompilationTaskBuilder.fromArgs(args);
        collectors.forEach(c -> c.onBeforeCompile(builder));

        JavaCompiler.CompilationTask task = builder.build();

        boolean success = task.call();

        CompilationReportData reportData = new CompilationReportData();

        collectors.forEach(c -> c.onAfterCompile(reportData));

        if (success) {}
        reporter.generateReport(reportData);
    }
}
