package io.github.eisopux.diagnostics.reporter;

import com.jetbrains.qodana.sarif.SarifUtil;
import com.jetbrains.qodana.sarif.model.ArtifactLocation;
import com.jetbrains.qodana.sarif.model.Level;
import com.jetbrains.qodana.sarif.model.Location;
import com.jetbrains.qodana.sarif.model.Message;
import com.jetbrains.qodana.sarif.model.PhysicalLocation;
import com.jetbrains.qodana.sarif.model.Region;
import com.jetbrains.qodana.sarif.model.ReportingDescriptor;
import com.jetbrains.qodana.sarif.model.Result;
import com.jetbrains.qodana.sarif.model.Run;
import com.jetbrains.qodana.sarif.model.SarifReport;
import com.jetbrains.qodana.sarif.model.Tool;
import com.jetbrains.qodana.sarif.model.ToolComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;

import io.github.eisopux.diagnostics.core.CompilationReportData;
import io.github.eisopux.diagnostics.core.Reporter;

/**
 * SarifReporter is a {@link Reporter} implementation that transforms the "diagnostics" section of a
 * {@link CompilationReportData} into a <a href="https://sarifweb.azurewebsites.net">SARIF</a> 2.1.0
 * log, using the {@code com.jetbrains.qodana:qodana-sarif} library as the object model.
 *
 * <p><b>This class is the entire integration surface with qodana-sarif.</b> No other file in this
 * project imports anything from {@code com.jetbrains.qodana.sarif.*}; every use of the library's
 * types is a local variable, parameter, or return value of a private method here. This is
 * deliberate: qodana-sarif is an experimental choice (see eisopux/javac-diagnostics-wrapper#152),
 * not published to Maven Central (see build.gradle's custom repository), and this project has
 * already tried multiple libraries for a given format before (see the various discarded options
 * documented in that issue) -- swapping to a different SARIF library later should mean rewriting
 * this one file, not hunting for scattered references elsewhere.
 *
 * <p>Only a small slice of the full SARIF object model is used: one {@code run} with a single
 * {@code tool.driver}, one {@code result} per collected diagnostic, and at most one {@code
 * location} per result. Fields the collected data has no equivalent for (code flows, fixes,
 * baselines, multiple runs, ...) are left out rather than populated with placeholder values.
 */
public class SarifReporter implements Reporter {

    /**
     * The {@code tool.driver.name} for every report. javac (not this wrapper, and not whichever
     * annotation processor happens to be attached) is the actual analysis tool being run; matches
     * the "javac" fallback {@link LspReporter} already uses for a diagnostic's {@code source}.
     */
    private static final String DRIVER_NAME = "javac";

    /**
     * javac's diagnostic code for any message an annotation processor issues through {@code
     * Messager} -- e.g. every error/warning the Checker Framework reports. Such a message's actual,
     * specific identity is a {@code [messageKey]} (optionally {@code [checker:messageKey]} under
     * {@code -AshowPrefixInWarningMessages}) prefix on the message text itself, not the {@code
     * code} field, which is this same generic value for every one of them regardless of checker or
     * kind of problem. Confirmed by running the Nullness Checker directly: the prefix is present
     * with or without that flag.
     */
    private static final Set<String> PROCESSOR_MESSAGE_CODES =
            new LinkedHashSet<>(
                    Arrays.asList(
                            "compiler.err.proc.messager",
                            "compiler.warn.proc.messager",
                            "compiler.note.proc.messager"));

    private static final Pattern MESSAGE_KEY_PREFIX = Pattern.compile("^\\[([^\\]]+)\\]\\s*");

    @Override
    public void generateReport(CompilationReportData reportData) {
        List<Map<String, Object>> diagnostics = reportData.getSection("diagnostics");
        if (diagnostics == null) {
            diagnostics = Collections.emptyList();
        }

        List<Result> results = new ArrayList<>();
        Set<String> ruleIds = new LinkedHashSet<>();
        for (Map<String, Object> diagnostic : diagnostics) {
            Result result = toResult(diagnostic);
            results.add(result);
            if (result.getRuleId() != null) {
                ruleIds.add(result.getRuleId());
            }
        }

        List<ReportingDescriptor> rules = new ArrayList<>();
        for (String ruleId : ruleIds) {
            rules.add(new ReportingDescriptor(ruleId));
        }

        ToolComponent driver = new ToolComponent(DRIVER_NAME).withRules(rules);
        Run run = new Run(new Tool(driver)).withResults(results);
        SarifReport sarifReport =
                new SarifReport(SarifReport.Version._2_1_0, Collections.singletonList(run));

        System.out.println(SarifUtil.createGson().toJson(sarifReport));
    }

    /** Converts one collected diagnostic (see DiagnosticCollector) into a SARIF result. */
    private static Result toResult(Map<String, Object> diagnostic) {
        String messageText = String.valueOf(diagnostic.get("message"));
        String ruleId = null;

        Object code = diagnostic.get("code");
        if (code != null && PROCESSOR_MESSAGE_CODES.contains(code.toString())) {
            Matcher keyMatcher = MESSAGE_KEY_PREFIX.matcher(messageText);
            if (keyMatcher.find()) {
                ruleId = keyMatcher.group(1);
                messageText = messageText.substring(keyMatcher.end());
            }
        }
        if (ruleId == null && code != null) {
            ruleId = code.toString();
        }

        Message message = new Message().withText(messageText);
        Result result = new Result(message).withLevel(toLevel(diagnostic.get("kind")));
        if (ruleId != null) {
            result = result.withRuleId(ruleId);
        }

        Location location = toLocation(diagnostic);
        if (location != null) {
            result = result.withLocations(Collections.singletonList(location));
        }

        return result;
    }

    /** Maps a javax.tools.Diagnostic.Kind to the closest SARIF result level. */
    private static Level toLevel(Object kind) {
        if (kind instanceof Diagnostic.Kind) {
            switch ((Diagnostic.Kind) kind) {
                case ERROR:
                    return Level.ERROR;
                case WARNING:
                case MANDATORY_WARNING:
                    return Level.WARNING;
                case NOTE:
                    return Level.NOTE;
                default:
                    break;
            }
        }
        return Level.NONE;
    }

    /**
     * Builds a location from the diagnostic's source file and, if available, a region within it.
     * Returns {@code null} if the diagnostic has no source ({@code DiagnosticCollector} stores
     * "unknown" in that case), since "unknown" is not a URI.
     */
    private static Location toLocation(Map<String, Object> diagnostic) {
        Object source = diagnostic.get("source");
        if (!(source instanceof String) || "unknown".equals(source)) {
            return null;
        }

        ArtifactLocation artifactLocation = new ArtifactLocation().withUri((String) source);
        PhysicalLocation physicalLocation =
                new PhysicalLocation().withArtifactLocation(artifactLocation);

        Region region = toRegion(diagnostic);
        if (region != null) {
            physicalLocation = physicalLocation.withRegion(region);
        }

        return new Location().withPhysicalLocation(physicalLocation);
    }

    /**
     * Builds a region from the diagnostic's line/column and character offset/length, omitting
     * whichever of those javac left as {@link Diagnostic#NOPOS} (or didn't set at all) rather than
     * inventing a value -- notably, no end line/column is guessed the way {@link LspReporter} must
     * for LSP's range model; charOffset/charLength already gives the precise span.
     */
    private static Region toRegion(Map<String, Object> diagnostic) {
        Integer line = toPositiveIntOrNull(diagnostic.get("lineNumber"));
        if (line == null) {
            return null;
        }
        Region region = new Region().withStartLine(line);

        Integer column = toPositiveIntOrNull(diagnostic.get("columnNumber"));
        if (column != null) {
            region = region.withStartColumn(column);
        }

        Integer startPosition = toPositiveIntOrNull(diagnostic.get("startPosition"));
        Integer endPosition = toPositiveIntOrNull(diagnostic.get("endPosition"));
        if (startPosition != null && endPosition != null && endPosition >= startPosition) {
            region =
                    region.withCharOffset(startPosition)
                            .withCharLength(endPosition - startPosition);
        }

        return region;
    }

    /**
     * Returns {@code value} as a positive {@code int}, or {@code null} if it isn't a {@link
     * Number}, or is {@link Diagnostic#NOPOS} (javac's marker for "no position available") or any
     * other non-positive value.
     */
    private static Integer toPositiveIntOrNull(Object value) {
        if (!(value instanceof Number)) {
            return null;
        }
        long longValue = ((Number) value).longValue();
        return longValue > 0 ? (int) longValue : null;
    }
}
