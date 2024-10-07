package io.github.wmdietl.diagnostics.json.javac;

import java.util.List;
import javax.tools.JavaFileObject;

import io.github.wmdietl.diagnostics.JavacDiagnosticsWrapper;
import io.github.wmdietl.diagnostics.json.common.Diagnostic;

/**
 * Wrapper around javac to output diagnostics as JSON, in a simple format directly corresponding to
 * the javac diagnostics.
 */
public class Main extends JavacDiagnosticsWrapper {
    public static void main(String[] args) {
        new Main().run(args);
    }

    /** Serialize the diagnostics using Gson. */
    @Override
    protected List<Diagnostic> processDiagnostics(List<javax.tools.Diagnostic<? extends JavaFileObject>> diagnostics) {
        JavacDiagnosticList diags = new JavacDiagnosticList(diagnostics);
        return diags.getDiagnostics();
    }
}
