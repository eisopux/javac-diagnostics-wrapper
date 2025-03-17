package org.eisopux.diagnostics.reporter;

import org.eisopux.diagnostics.core.CompilationTaskBuilder;
import org.eisopux.diagnostics.core.Reporter;

import java.util.Map;

public class ConsoleReporter implements Reporter {

    @Override
    public void generateReport(CompilationTaskBuilder.CompilationReportData reportData) {
        reportData.getAllSections().forEach((sectionName, sectionData) -> {
            System.out.println("Section: " + sectionName);

            if (sectionData instanceof Map) {
                ((Map<?, ?>) sectionData).forEach((key, value) ->
                        System.out.println("  " + key + ": " + value)
                );
            } else if (sectionData instanceof Iterable) {
                for (Object element : (Iterable<?>) sectionData) {
                    System.out.println("  " + element);
                }
            } else {
                System.out.println("  " + sectionData);
            }

            System.out.println();
        });
    }
}
