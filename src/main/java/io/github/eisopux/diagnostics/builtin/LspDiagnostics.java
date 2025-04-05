package io.github.eisopux.diagnostics.builtin;

import io.github.eisopux.diagnostics.collectors.DiagnosticCollector;
import io.github.eisopux.diagnostics.core.CompilerRunner;
import io.github.eisopux.diagnostics.reporter.LspReporter;

/** A prebuilt LSP diagnostics output to call from the command line */
public class LspDiagnostics {
    public static void main(String[] args) {
        CompilerRunner runner =
                new CompilerRunner()
                        .addCollector(new DiagnosticCollector())
                        .setReporter(new LspReporter());

        runner.run(args);
    }
}
