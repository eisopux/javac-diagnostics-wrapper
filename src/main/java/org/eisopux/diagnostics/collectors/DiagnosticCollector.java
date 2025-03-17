package org.eisopux.diagnostics.collectors;

import org.eisopux.diagnostics.core.Collector;
import org.eisopux.diagnostics.core.CompilationTaskBuilder;
import org.eisopux.diagnostics.utility.CompilationReportData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * A concrete implementation of {@link Collector} that wraps a {@link
 * javax.tools.DiagnosticCollector} to capture compilation diagnostics.
 */
public class DiagnosticCollector implements Collector<Diagnostic<? extends JavaFileObject>> {

    private final javax.tools.DiagnosticCollector<JavaFileObject> diagCollector =
            new javax.tools.DiagnosticCollector<>();

    private List<Diagnostic<? extends JavaFileObject>> finalDiagnostics;

    @Override
    public void onBeforeCompile(CompilationTaskBuilder builder) {
        builder.addDiagnosticListener(diagCollector);
    }

    @Override
    public void onAfterCompile(CompilationReportData reportData) {
        this.finalDiagnostics = diagCollector.getDiagnostics();

        if (finalDiagnostics != null) {
            List<Map<String, Object>> details = finalDiagnostics.stream().map(diag -> {
                Map<String, Object> diagMap = new HashMap<>();
                diagMap.put("source", diag.getSource() != null ? diag.getSource().toUri().toString() : null);
                diagMap.put("kind", diag.getKind());
                diagMap.put("position", diag.getPosition());
                diagMap.put("startPosition", diag.getStartPosition());
                diagMap.put("endPosition", diag.getEndPosition());
                diagMap.put("lineNumber", diag.getLineNumber());
                diagMap.put("columnNumber", diag.getColumnNumber());
                diagMap.put("code", diag.getCode());
                diagMap.put("message", diag.getMessage(null));
                return diagMap;
            }).collect(Collectors.toList());

            reportData.putSection("diagnostics", details);
        }
    }


    @Override
    public List<Diagnostic<? extends JavaFileObject>> getItems() {
        return finalDiagnostics;
    }
}
