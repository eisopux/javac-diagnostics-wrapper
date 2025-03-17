package org.eisopux.diagnostics.collectors;

import org.eisopux.diagnostics.core.Collector;
import org.eisopux.diagnostics.core.CompilationTaskBuilder;

import java.util.List;

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
    public void onAfterCompile() {
        // Finalize diagnostics.
        this.finalDiagnostics = diagCollector.getDiagnostics();

        //        // Prepare a section (as a Map) for diagnostics.
        //        Map<String, Object> sectionData = new HashMap<>();
        //        int count = finalDiagnostics != null ? finalDiagnostics.size() : 0;
        //        sectionData.put("diagnosticCount", count);
        //        if (finalDiagnostics != null) {
        //            // Build a list of diagnostic details.
        //            List<Map<String, Object>> details = finalDiagnostics.stream().map(diag -> {
        //                Map<String, Object> diagMap = new HashMap<>();
        //                // Define keys for the reporter to understand.
        //                diagMap.put("message", diag.getMessage(null));
        //                diagMap.put("lineNumber", diag.getLineNumber());
        //                // Additional keys can be added here.
        //                return diagMap;
        //            }).collect(Collectors.toList());
        //            sectionData.put("diagnostics", details);
        //        }
        //        // Insert this section into the central report using a unique identifier.
        //        reportData.putSection("diagnostics", sectionData);
    }

    @Override
    public List<Diagnostic<? extends JavaFileObject>> getItems() {
        return finalDiagnostics;
    }
}
