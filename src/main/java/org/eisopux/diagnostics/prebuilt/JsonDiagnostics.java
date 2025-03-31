package org.eisopux.diagnostics.prebuilt;

import org.eisopux.diagnostics.collectors.DiagnosticCollector;
import org.eisopux.diagnostics.core.CompilerRunner;
import org.eisopux.diagnostics.reporter.JSONReporter;

/** A prebuilt JSON diagnostics output to call from the command line */
public class JsonDiagnostics {
    public static void main(String[] args) {
        CompilerRunner runner =
                new CompilerRunner()
                        .addCollector(new DiagnosticCollector())
                        .setReporter(new JSONReporter());

        runner.run(args);
    }
}
