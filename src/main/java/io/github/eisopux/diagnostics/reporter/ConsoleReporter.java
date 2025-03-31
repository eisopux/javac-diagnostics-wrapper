package io.github.eisopux.diagnostics.reporter;

import io.github.eisopux.diagnostics.core.CompilationReportData;
import io.github.eisopux.diagnostics.core.Reporter;

/**
 * ConsoleReporter is an implementation of {@link Reporter} that
 * outputs unformatted report data to the console.
 */
public class ConsoleReporter implements Reporter {

    @Override
    public void generateReport(CompilationReportData reportData) {
        reportData
                .getAllSections()
                .forEach(
                        (sectionName, sectionData) -> {
                            System.out.println("Section: " + sectionName);
                            System.out.println(sectionData);
                            System.out.println(); // Blank line between sections.
                        });
    }
}
