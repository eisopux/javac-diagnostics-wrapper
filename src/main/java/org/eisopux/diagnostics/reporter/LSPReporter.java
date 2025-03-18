package org.eisopux.diagnostics.reporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eisopux.diagnostics.core.CompilationReportData;
import org.eisopux.diagnostics.core.Reporter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * LSPReporter is a {@link org.eisopux.diagnostics.core.Reporter} implementation that transforms
 * aggregated compilation report data into a JSON output format compliant with the Language Server
 * Protocol (LSP) diagnostic standard.
 *
 * <p>To extend this reporter to support additional sections from other collectors, add
 * corresponding cases to the switch statement in {@link #generateReport(CompilationReportData)}.
 * For each new section, implement a conversion method named {@code
 * generateSectionNameReport(List<Map<String, Object>> sectionData)} (e.g., {@code
 * generatePerformanceReport}) to transform that section's data into the LSP-compliant format.
 *
 * <p>For details on the diagnostic format, please refer to the <a
 * href="https://microsoft.github.io/language-server-protocol/specifications/specification-current/">
 * Language Server Protocol Specification</a>.
 */
public class LSPReporter implements Reporter {

    private static final Pattern PROCESSOR_PATTERN =
            Pattern.compile("^\\[([^:\\]]+)(?::[^\\]]+)?\\]");

    @Override
    public void generateReport(CompilationReportData reportData) {

        Map<String, List<Map<String, Object>>> allSections = reportData.getAllSections();

        Map<String, Object> transformedOutput = new LinkedHashMap<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : allSections.entrySet()) {
            String sectionId = entry.getKey();
            List<Map<String, Object>> sectionData = entry.getValue();
            Object transformed;

            switch (sectionId) {
                case "diagnostics":
                    transformed = generateDiagnosticsReport(sectionData);
                    break;
                // Future sections can have their own cases here.
                default:
                    // For sections without a specific transformation, keep the data as is.
                    transformed = sectionData;
            }

            transformedOutput.put(sectionId, transformed);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(transformedOutput);
        System.out.println(json);
    }

    /**
     * Groups diagnostics by the file URI (stored in the "source" key) and produces an LSP-compliant
     * output.
     */
    private List<Map<String, Object>> generateDiagnosticsReport(
            List<Map<String, Object>> diagnosticsList) {
        Map<String, List<Map<String, Object>>> grouped =
                diagnosticsList.stream()
                        .collect(
                                Collectors.groupingBy(
                                        diag -> {
                                            Object fileUriObj = diag.get("source");
                                            return fileUriObj != null
                                                    ? fileUriObj.toString()
                                                    : "unknown";
                                        }));

        List<Map<String, Object>> output = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            String fileUri = entry.getKey();
            List<Map<String, Object>> lspDiagnostics =
                    entry.getValue().stream()
                            .map(this::transformDiagnosticToLSP)
                            .collect(Collectors.toList());

            Map<String, Object> fileEntry = new LinkedHashMap<>();
            fileEntry.put("uri", fileUri);
            fileEntry.put("diagnostics", lspDiagnostics);
            output.add(fileEntry);
        }
        return output;
    }

    /** Transforms a single diagnostic map into the LSP diagnostic format. */
    private Map<String, Object> transformDiagnosticToLSP(Map<String, Object> diag) {
        Map<String, Object> lspDiag = new LinkedHashMap<>();

        int line =
                diag.get("lineNumber") != null
                        ? ((Number) diag.get("lineNumber")).intValue() - 1
                        : 0;
        int column =
                diag.get("columnNumber") != null
                        ? ((Number) diag.get("columnNumber")).intValue() - 1
                        : 0;
        int endColumn = column + 2; // Assume a fixed width; adjust as needed.

        lspDiag.put("range", createRange(line, column, endColumn));

        String kind = diag.get("kind") != null ? diag.get("kind").toString() : "";
        int severity = DiagnosticKind.fromString(kind);
        lspDiag.put("severity", severity);

        lspDiag.put("code", diag.get("code"));
        lspDiag.put("message", diag.get("message"));

        String processorName = extractProcessorFromMessage(diag.get("message"));
        lspDiag.put("source", processorName != null ? processorName : "javac");

        return lspDiag;
    }

    /** Helper to create an LSP range object. */
    private Map<String, Object> createRange(int line, int column, int endColumn) {
        Map<String, Object> start = new LinkedHashMap<>();
        start.put("line", line);
        start.put("character", column);

        Map<String, Object> end = new LinkedHashMap<>();
        end.put("line", line);
        end.put("character", endColumn);

        Map<String, Object> range = new LinkedHashMap<>();
        range.put("start", start);
        range.put("end", end);
        return range;
    }

    /** Extracts the processor name from the diagnostic message using a regex. */
    private String extractProcessorFromMessage(Object messageObj) {
        if (messageObj == null) {
            return null;
        }
        String message = messageObj.toString();
        Matcher matcher = PROCESSOR_PATTERN.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** Nested enum for mapping diagnostic kinds to LSP severity values. */
    private enum DiagnosticKind {
        ERROR(1),
        WARNING(2),
        INFORMATION(3),
        HINT(4);

        private final int lspSeverity;

        DiagnosticKind(int severity) {
            this.lspSeverity = severity;
        }

        public static int fromString(String kindStr) {
            if (kindStr == null) {
                return INFORMATION.lspSeverity;
            }
            try {
                return DiagnosticKind.valueOf(kindStr.toUpperCase(Locale.ROOT)).lspSeverity;
            } catch (IllegalArgumentException e) {
                return INFORMATION.lspSeverity;
            }
        }
    }
}
