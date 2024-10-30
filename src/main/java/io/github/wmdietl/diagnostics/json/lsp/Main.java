package io.github.wmdietl.diagnostics.json.lsp;

import java.util.List;

import javax.tools.JavaFileObject;

import io.github.wmdietl.diagnostics.JavacDiagnosticsWrapper;
import io.github.wmdietl.diagnostics.common.DiagnosticList;

/**
 * Wrapper around javac to output diagnostics as JSON, in a simple format directly corresponding to
 * the javac diagnostics.
 */
public class Main extends JavacDiagnosticsWrapper {
    public static void main(String[] args) {
        new Main().run(args);
    }

    /** Serialize the diagnostics using the LSP format. */
    @Override
    protected DiagnosticList processDiagnostics(
            List<javax.tools.Diagnostic<? extends JavaFileObject>> diagnostics) {
        return new LspDiagnosticList(diagnostics);
    }
}
