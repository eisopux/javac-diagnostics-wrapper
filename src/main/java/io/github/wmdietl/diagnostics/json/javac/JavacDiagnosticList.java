package io.github.wmdietl.diagnostics.json.javac;

import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;

public class JavacDiagnosticList {

    // The list that contains all diagnostics
    public final List<JavacDiagnostic> diagnostics;

    public JavacDiagnosticList(List<javax.tools.Diagnostic<? extends JavaFileObject>> diags) {
        diagnostics = convertStandardDiagnostics(diags);
    }

    private List<JavacDiagnostic> convertStandardDiagnostics(
            List<javax.tools.Diagnostic<? extends JavaFileObject>> diags) {
        List<JavacDiagnostic> processed = new ArrayList<>();
        for (javax.tools.Diagnostic<? extends JavaFileObject> d : diags) {
            processed.add(new JavacDiagnostic(d));
        }
        return processed;
    }
}
