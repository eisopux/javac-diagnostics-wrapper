package io.github.eisopux.diagnostics.builtin;

import io.github.eisopux.diagnostics.collectors.DiagnosticCollector;
import io.github.eisopux.diagnostics.core.CompilerRunner;
import io.github.eisopux.diagnostics.reporter.SarifReporter;

/**
 * A prebuilt SARIF diagnostics output to call from the command line.
 *
 * <p>Experimental: see {@link SarifReporter}'s class Javadoc.
 */
public class SarifDiagnostics {
    public static void main(String[] args) {
        CompilerRunner runner =
                new CompilerRunner()
                        .addCollector(new DiagnosticCollector())
                        .setReporter(new SarifReporter());

        System.exit(runner.run(args) ? 0 : 1);
    }
}
