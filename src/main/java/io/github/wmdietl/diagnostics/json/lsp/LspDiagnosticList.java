package io.github.wmdietl.diagnostics.json.lsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;

import io.github.wmdietl.diagnostics.common.Diagnostic;
import io.github.wmdietl.diagnostics.common.DiagnosticList;

public class LspDiagnosticList extends DiagnosticList {
    public LspDiagnosticList(List<javax.tools.Diagnostic<? extends JavaFileObject>> diags) {
        // When this object is created, delegate construction to the constructor of the super class
        super(diags);
    }

    @Override
    protected List<Diagnostic> convertOrg(
            List<javax.tools.Diagnostic<? extends JavaFileObject>> diags) {
        // Mapping from unique URIs to the diagnostics for that URI
        Map<String, List<LspDiagnostic.Diagnostic>> fileDiagnostics = new HashMap<>();
        for (javax.tools.Diagnostic<? extends JavaFileObject> d : diags) {
            // obtain the source of current diagnostic
            JavaFileObject file = d.getSource();
            String source = file != null ? file.toUri().toString() : "unknown file";
            // add a new list to a file if the file hasn't been processed before
            if (!fileDiagnostics.containsKey(source)) {
                fileDiagnostics.put(source, new ArrayList<>());
            }
            // d is a javax.tools.Diagnostic, and we want to convert to a Diagnostics that we
            // defined
            fileDiagnostics.get(source).add(new LspDiagnostic.Diagnostic(d));
        }

        // Convert to FileDiagnostics for JSON identifiers
        List<Diagnostic> jsonDiagnostics = new ArrayList<>();
        for (Map.Entry<String, List<LspDiagnostic.Diagnostic>> entry : fileDiagnostics.entrySet()) {
            jsonDiagnostics.add(new LspDiagnostic(entry.getKey(), entry.getValue()));
        }
        return jsonDiagnostics;
    }
}