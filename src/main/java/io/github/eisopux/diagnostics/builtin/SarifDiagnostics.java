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
    /**
     * Runs javac over {@code args} (the same command-line arguments javac itself accepts) and
     * prints the resulting diagnostics as a SARIF log.
     *
     * @param args javac command-line arguments, e.g. source files and {@code -classpath}/{@code
     *     -processor} options
     */
    public static void main(String[] args) {
        CompilerRunner runner =
                new CompilerRunner()
                        .addCollector(new DiagnosticCollector())
                        .setReporter(new SarifReporter());

        System.exit(runner.run(args) ? 0 : 1);
    }
}
