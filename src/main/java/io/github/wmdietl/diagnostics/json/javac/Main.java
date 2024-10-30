package io.github.wmdietl.diagnostics.json.javac;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import io.github.wmdietl.diagnostics.JavacDiagnosticsWrapper;

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
    protected void processDiagnostics(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        JsonDiagnosticList diags = new JsonDiagnosticList(diagnostics);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(diags, JsonDiagnosticList.class));
    }
}
