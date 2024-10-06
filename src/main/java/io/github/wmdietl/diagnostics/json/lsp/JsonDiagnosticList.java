package io.github.wmdietl.diagnostics.json.lsp;

import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;
import javax.tools.Diagnostic;

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
