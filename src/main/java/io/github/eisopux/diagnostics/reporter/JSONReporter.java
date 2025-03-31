package io.github.eisopux.diagnostics.reporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.eisopux.diagnostics.core.CompilationReportData;
import io.github.eisopux.diagnostics.core.Reporter;

/**
 * JSONReporter is an implementation of {@link Reporter} that
 * faithfully outputs each {@link CompilationReportData} section as
 * JSON.
 */
public class JSONReporter implements Reporter {

    @Override
    public void generateReport(CompilationReportData reportData) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(reportData.getAllSections());
        System.out.println(json);
    }
}
