package org.eisopux.diagnostics.reporter;

import org.eisopux.diagnostics.core.Reporter;
import org.eisopux.diagnostics.utility.CompilationReportData;

public class ConsoleReporter implements Reporter {

    @Override
    public void generateReport(CompilationReportData reportData) {
        System.out.println("=== Compilation Report ===");
        // Iterate over each section contributed by collectors.
        reportData
                .getAllSections()
                .forEach(
                        (sectionName, sectionData) -> {
                            System.out.println("Section: " + sectionName);
                            sectionData.forEach(
                                    (key, value) -> {
                                        System.out.println("  " + key + ": " + value);
                                    });
                            System.out.println(); // Blank line between sections.
                        });
    }
}
