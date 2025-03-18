package org.eisopux.diagnostics.prebuilt;

import org.eisopux.diagnostics.collectors.DiagnosticCollector;
import org.eisopux.diagnostics.core.CompilerRunner;
import org.eisopux.diagnostics.reporter.LSPReporter;

/** A prebuilt LSP diagnostics output to call from the command line */
public class LSPDiagnostics {
    public static void main(String[] args) {
        CompilerRunner runner =
                new CompilerRunner()
                        .addCollector(new DiagnosticCollector())
                        .setReporter(new LSPReporter());

        runner.run(args);
    }
}
