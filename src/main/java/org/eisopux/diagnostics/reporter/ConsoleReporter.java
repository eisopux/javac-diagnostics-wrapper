package org.eisopux.diagnostics.reporter;

import org.eisopux.diagnostics.core.Collector;
import org.eisopux.diagnostics.core.Reporter;

import java.util.List;

public class ConsoleReporter implements Reporter {

    @Override
    public void generateReport(List<? extends Collector<?>> collectors) {
        System.out.println("=== Compilation Report ===");

        for (Collector<?> collector : collectors) {
            System.out.println(collector.getItems());
        }
        //        // Iterate over each section contributed by collectors.
        //        reportData.getAllSections().forEach((sectionName, sectionData) -> {
        //            System.out.println("Section: " + sectionName);
        //            sectionData.forEach((key, value) -> {
        //                System.out.println("  " + key + ": " + value);
        //            });
        //            System.out.println(); // Blank line between sections.
        //        });
    }
}
