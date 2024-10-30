package io.github.wmdietl.diagnostics.json.lsp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;

import io.github.wmdietl.diagnostics.JavacDiagnosticsWrapper;
import io.github.wmdietl.diagnostics.json.common.*;

/** Wrapper around javac to output diagnostics as JSON, in the LSP format. */
public class Main extends JavacDiagnosticsWrapper {
    public static void main(String[] args) {
        new Main().run(args);
    }

    /** Serialize the diagnostics using the LSP format. */
    @Override
    protected void processDiagnostics(List<javax.tools.Diagnostic<? extends JavaFileObject>> diagnostics) {
        // Mapping from unique URIs to the diagnostics for that URI
        Map<String, List<Diagnostic>> fileDiagnostics = new HashMap<>();
        for (javax.tools.Diagnostic<? extends JavaFileObject> d : diagnostics) {
            JavaFileObject file = d.getSource();
            String source = file != null ? file.toUri().toString() : "unknown file";
            if (!fileDiagnostics.containsKey(source)) {
                fileDiagnostics.put(source, new ArrayList<>());
            }
            // d is a javax.tools.Diagnostic, and we want to convert to a Diagnostics that we defined
            fileDiagnostics.get(source).add(convert(d));
        }

        // Convert to FileDiagnostics for JSON identifiers
        List<FileDiagnostics> jsonDiagnostics = new ArrayList<>();
        for (Map.Entry<String, List<Diagnostic>> entry : fileDiagnostics.entrySet()) {
            jsonDiagnostics.add(new FileDiagnostics(entry.getKey(), entry.getValue()));
        }

        // Write JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(jsonDiagnostics));
    }

    private Diagnostic convert(javax.tools.Diagnostic<? extends JavaFileObject> diagnostic) {
        DiagnosticSeverity severity;
        switch (diagnostic.getKind()) {
            case ERROR:
                severity = DiagnosticSeverity.ERROR;
                break;
            case WARNING:
            case MANDATORY_WARNING:
                severity = DiagnosticSeverity.WARNING;
                break;
            case NOTE:
            case OTHER:
                severity = DiagnosticSeverity.INFORMATION;
                break;
            default:
                throw new IllegalArgumentException("Unexpected diagnostic kind in: " + diagnostic);
        }

        int line = (int) diagnostic.getLineNumber();
        int column = (int) diagnostic.getColumnNumber();
        Range range;
        if (line < 1 || column < 1) {
            // Use beginning of document for invalid locations
            range = new Range(Position.START, Position.START);
        } else {
            // javac is 1-based whereas LSP is 0-based
            Position start = new Position(line - 1, column - 1);
            Position end =
                    new Position(
                            line - 1,
                            (int)
                                    (column
                                            - 1
                                            + diagnostic.getEndPosition()
                                            - diagnostic.getStartPosition()));
            range = new Range(start, end);
        }

        return new Diagnostic(
                range,
                severity.value,
                diagnostic.getCode(),
                this.getClass().getCanonicalName(),
                diagnostic.getMessage(null),
                null);
    }
}
