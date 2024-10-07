package io.github.wmdietl.diagnostics.json.lsp;

import java.util.List;
import javax.tools.JavaFileObject;

import io.github.wmdietl.diagnostics.JavacDiagnosticsWrapper;
import io.github.wmdietl.diagnostics.json.common.Diagnostic;

/** Wrapper around javac to output diagnostics as JSON, in the LSP format. */
public class Main extends JavacDiagnosticsWrapper {
    public static void main(String[] args) {
        new Main().run(args);
    }

    /** Serialize the diagnostics using the LSP format. */
    @Override
    protected List<Diagnostic> processDiagnostics(List<javax.tools.Diagnostic<? extends JavaFileObject>> diagnostics) {
        LspDiagnosticList diags = new LspDiagnosticList(diagnostics);
        return diags.getDiagnostics();
    }
}
