package org.eisopux.diagnostics.core;

import java.util.ArrayList;
import java.util.List;

import javax.tools.*;

/** A runner that sets up the javac task, attaches collectors, and runs the compilation. */
public class CompilerRunner {

    private final List<Collector<?>> collectors = new ArrayList<>();
    private Reporter reporter;

    public CompilerRunner addCollector(Collector<?> collector) {
        this.collectors.add(collector);
        return this;
    }

    public CompilerRunner setReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }

    public void run(String[] args) {

        CompilationTaskBuilder builder = CompilationTaskBuilder.fromArgs(args);
        collectors.forEach(c -> c.onBeforeCompile(builder));

        JavaCompiler.CompilationTask task = builder.build();

        boolean success = task.call();

        CompilationReportData reportData =
                new CompilationReportData();

        collectors.forEach(c -> c.onAfterCompile(reportData));

        if (success) {}
        reporter.generateReport(reportData);
    }
}
