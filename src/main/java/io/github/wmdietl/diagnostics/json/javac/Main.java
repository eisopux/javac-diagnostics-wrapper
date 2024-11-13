package io.github.wmdietl.diagnostics.json.javac;

import java.util.List;

import javax.tools.JavaFileObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.wmdietl.diagnostics.JavacDiagnosticsWrapper;
import io.github.wmdietl.diagnostics.common.DiagnosticList;

/**
 * Wrapper around javac to output diagnostics as JSON, in a simple format directly corresponding to
 * the javac diagnostics.
 */
public class Main extends JavacDiagnosticsWrapper {
    public static void main(String[] args) {
        new Main().run(args);
    }

    /** Serialize the diagnostics using Gson. */
    @Override
    protected void processDiagnostics(
            List<javax.tools.Diagnostic<? extends JavaFileObject>> diagnostics) {
        DiagnosticList list = new JavacDiagnosticList(diagnostics);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(list.getDiagnostics()));
    }
}
