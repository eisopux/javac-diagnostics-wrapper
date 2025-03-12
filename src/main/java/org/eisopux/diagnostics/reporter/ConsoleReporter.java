package org.eisopux.diagnostics.reporter;

import org.eisopux.diagnostics.core.Collector;
import org.eisopux.diagnostics.core.Reporter;

import java.util.List;

/** A very simple reporter that outputs raw collector data to the console. */
public class ConsoleReporter implements Reporter {

    @Override
    public void generateReport(List<? extends Collector<?>> collectors) {
        for (Collector<?> collector : collectors) {
            System.out.println(collector.getItems());
        }
    }
}
