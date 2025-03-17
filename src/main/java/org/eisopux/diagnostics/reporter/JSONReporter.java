package org.eisopux.diagnostics.reporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eisopux.diagnostics.core.Reporter;
import org.eisopux.diagnostics.utility.CompilationReportData;

public class JSONReporter implements Reporter {

    @Override
    public void generateReport(CompilationReportData reportData) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(reportData.getAllSections());
        System.out.println(json);
    }
}



