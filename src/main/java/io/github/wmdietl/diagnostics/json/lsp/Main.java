package io.github.wmdietl.diagnostics.json.lsp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;

import io.github.wmdietl.diagnostics.JavacDiagnosticsWrapper;
import io.github.wmdietl.diagnostics.json.common.Diagnostic;
import io.github.wmdietl.diagnostics.json.common.DiagnosticSeverity;
import io.github.wmdietl.diagnostics.json.common.FileDiagnostics;
import io.github.wmdietl.diagnostics.json.common.Position;
import io.github.wmdietl.diagnostics.json.common.Range;

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

    /**
     * Convert from standard javac Diagnostic to self-defined Diagnostic object
     * 
     * @param diagnostic javac standard diagnostic
     * @return self-defined diagnostic object
     */
    private Diagnostic convert(javax.tools.Diagnostic<? extends JavaFileObject> diagnostic) {
        // Convert from javac severity to self-defined severity
        DiagnosticSeverity severity = DiagnosticSeverity.convert(diagnostic.getKind());
        // Convert from javac error locations to self-defined range
        Range range = convertRange(
            diagnostic.getLineNumber(), diagnostic.getColumnNumber(), diagnostic.getStartPosition(), diagnostic.getEndPosition());
        // Construct the diagnostic object
        return new Diagnostic(
                range,
                severity.value,
                diagnostic.getCode(),
                this.getClass().getCanonicalName(),
                diagnostic.getMessage(null),
                null);
    }

    private static Range convertRange(final long line, final long column, final long startPos, final long endPos) {
        if (line < 1 || column < 1){
            return new Range(Position.START, Position.START);
        }
        // javac is 1-based whereas LSP is 0-based
        Position start = new Position(line - 1, column - 1);
        Position end   = new Position(line - 1, (column - 1 + endPos - startPos));
        return new Range(start, end);
    }
}
