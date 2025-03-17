package org.eisopux.diagnostics.reporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eisopux.diagnostics.core.CompilationTaskBuilder;
import org.eisopux.diagnostics.core.Reporter;

import java.util.*;
import java.util.stream.Collectors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LSPReporter implements Reporter {

    @Override
    public void generateReport(CompilationTaskBuilder.CompilationReportData reportData) {
        Object sectionObj = reportData.getSection("diagnostics");
        if (!(sectionObj instanceof List)) {
            System.out.println("[]");
            return;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> diagnosticsList = (List<Map<String, Object>>) sectionObj;

        List<Map<String, Object>> lspOutput = generateDiagnosticsReport(diagnosticsList);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(lspOutput);
        System.out.println(json);
    }

    private List<Map<String, Object>> generateDiagnosticsReport(List<Map<String, Object>> diagnosticsList) {
        Map<String, List<Map<String, Object>>> groupedDiagnostics = diagnosticsList.stream()
                .collect(Collectors.groupingBy(diag -> {
                    Object fileUriObj = diag.get("source");
                    return fileUriObj != null ? fileUriObj.toString() : "unknown";
                }));

        List<Map<String, Object>> output = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedDiagnostics.entrySet()) {
            String fileUri = entry.getKey();
            List<Map<String, Object>> lspDiagnostics = entry.getValue().stream()
                    .map(this::transformDiagnosticToLSP)
                    .collect(Collectors.toList());

            Map<String, Object> fileEntry = new LinkedHashMap<>();
            fileEntry.put("uri", fileUri);
            fileEntry.put("diagnostics", lspDiagnostics);
            output.add(fileEntry);
        }
        return output;
    }

    private Map<String, Object> transformDiagnosticToLSP(Map<String, Object> diag) {
        Map<String, Object> lspDiag = new LinkedHashMap<>();

        // Convert 1-indexed line/column to 0-indexed.
        int line = diag.get("lineNumber") != null ? ((Number) diag.get("lineNumber")).intValue() - 1 : 0;
        int column = diag.get("columnNumber") != null ? ((Number) diag.get("columnNumber")).intValue() - 1 : 0;
        int endColumn = column + 2;  // Assume a fixed width; adjust as needed.

        Map<String, Object> start = new LinkedHashMap<>();
        start.put("line", line);
        start.put("character", column);
        Map<String, Object> end = new LinkedHashMap<>();
        end.put("line", line);
        end.put("character", endColumn);
        Map<String, Object> range = new LinkedHashMap<>();
        range.put("start", start);
        range.put("end", end);
        lspDiag.put("range", range);

        String kind = diag.get("kind") != null ? diag.get("kind").toString() : "";
        int severity = DiagnosticKind.fromString(kind);
        lspDiag.put("severity", severity);

        lspDiag.put("code", diag.get("code"));
        lspDiag.put("message", diag.get("message"));

        // Set source to "javac" as a placeholder.
        String processorName = extractProcessorFromMessage(diag.get("message"));
        if (processorName == null) {
            processorName = "javac";
        }
        lspDiag.put("source", processorName);

        return lspDiag;
    }

    private static final Pattern PROCESSOR_PATTERN = Pattern.compile("^\\[([^:\\]]+)(?::[^\\]]+)?\\]");

    private String extractProcessorFromMessage(Object messageObj) {
        if (messageObj == null) {
            return null;
        }
        String message = messageObj.toString();
        Matcher matcher = PROCESSOR_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


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
