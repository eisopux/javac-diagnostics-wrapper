package io.github.eisopux.diagnostics.collectors;

import io.github.eisopux.diagnostics.core.Collector;
import io.github.eisopux.diagnostics.core.CompilationReportData;
import io.github.eisopux.diagnostics.core.CompilationTaskBuilder;

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
public class DiagnosticCollector implements Collector {

    private final javax.tools.DiagnosticCollector<JavaFileObject> diagCollector =
            new javax.tools.DiagnosticCollector<>();

    @Override
    public void onBeforeCompile(CompilationTaskBuilder builder) {
        builder.addDiagnosticListener(diagCollector);
    }

    @Override
    public void onAfterCompile(CompilationReportData reportData) {
        List<Diagnostic<? extends JavaFileObject>> finalDiagnostics =
                diagCollector.getDiagnostics();

        List<Map<String, Object>> details =
                finalDiagnostics.stream()
                        .map(
                                diag -> {
                                    Map<String, Object> diagMap = new HashMap<>();
                                    diagMap.put("message", diag.getMessage(null));
                                    diagMap.put("lineNumber", diag.getLineNumber());
                                    diagMap.put("columnNumber", diag.getColumnNumber());
                                    diagMap.put("kind", diag.getKind());
                                    diagMap.put("code", diag.getCode());
                                    diagMap.put(
                                            "source",
                                            diag.getSource() != null
                                                    ? diag.getSource().toUri().toString()
                                                    : "unknown");
                                    diagMap.put("position", diag.getPosition());
                                    diagMap.put("startPosition", diag.getStartPosition());
                                    diagMap.put("endPosition", diag.getEndPosition());
                                    return diagMap;
                                })
                        .collect(Collectors.toList());

        reportData.putSection("diagnostics", details);
    }
}
