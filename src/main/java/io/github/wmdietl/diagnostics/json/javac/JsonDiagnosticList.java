package io.github.wmdietl.diagnostics.json.javac;

import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * JSON wrapper class. Fields are encoded in the JSON output.
 * Define a list of json entries
 */
public class JsonDiagnosticList {

    private final List<JsonDiagnostic> diagnostics;

    public JsonDiagnosticList(List<Diagnostic<? extends JavaFileObject>> diags) {
        diagnostics = new ArrayList<>(diags.size());
        for (Diagnostic<? extends JavaFileObject> d : diags) {
            diagnostics.add(new JsonDiagnostic(d));
        }
    }

    public List<JsonDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
