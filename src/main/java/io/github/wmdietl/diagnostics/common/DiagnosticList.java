package io.github.wmdietl.diagnostics.common;

import java.util.List;
import javax.tools.JavaFileObject;

public abstract class DiagnosticList {

    // The list that contains all diagnostics in a specific format
    private final List<Diagnostic> diagnostics;

    public DiagnosticList(List<javax.tools.Diagnostic<? extends JavaFileObject>> diags) {
        diagnostics = convertOrg(diags);
    }

    // Obtain the list of diagnostics in already processed form
    public List<Diagnostic> getDiagnostics(){
        return diagnostics;
    }

    // Convert from standard javac diagnostic to a specific format
    protected abstract List<Diagnostic> convertOrg(List<javax.tools.Diagnostic<? extends JavaFileObject>> diags);
}
