package org.eisopux.diagnostics.reporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eisopux.diagnostics.core.CompilationReportData;
import org.eisopux.diagnostics.core.Reporter;

/**
 * JSONReporter is an implementation of {@link org.eisopux.diagnostics.core.Reporter} that
 * faithfully outputs each {@link org.eisopux.diagnostics.core.CompilationReportData} section as
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
