package org.eisopux.diagnostics.collectors;

import org.eisopux.diagnostics.core.ConsoleRenderable;
import org.eisopux.diagnostics.core.JavacCollector;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.util.List;

/**
 * A concrete implementation of {@link JavacCollector} that wraps a
 * {@link DiagnosticCollector} to capture compilation diagnostics.
 */
public class DiagnosticCollectorImpl implements JavacCollector, ConsoleRenderable {

    // The underlying DiagnosticCollector that javac needs.
    private final DiagnosticCollector<JavaFileObject> diagCollector =
            new DiagnosticCollector<>();

    // After compilation finishes, we'll store the final list of diagnostics here.
    private List<Diagnostic<? extends JavaFileObject>> finalDiagnostics;

    /**
     * Called before javac runs. In this example, we don't need special setup,
     * so it's a no-op. But it gives us a hook if needed.
     */
    @Override
    public void onBeforeCompile() {
        // No pre-compile logic needed here.
    }

    /**
     * Called after javac completes. Retrieves the aggregated diagnostics
     * from the underlying DiagnosticCollector.
     *
     * @param success true if compilation succeeded, false otherwise
     */
    @Override
    public void onAfterCompile(boolean success) {
        // Gather final diagnostics
        finalDiagnostics = diagCollector.getDiagnostics();
    }

    /**
     * Expose the raw {@link DiagnosticCollector} so that a runner can pass it to
     * javac's getTask(...) method.
     */
    public DiagnosticCollector<JavaFileObject> getUnderlyingCollector() {
        return diagCollector;
    }

    /**
     * Return the final list of diagnostics after the compilation has finished.
     */
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return finalDiagnostics;
    }

    @Override
    public String toConsoleString() {
        if (finalDiagnostics == null || finalDiagnostics.isEmpty()) {
            return "No diagnostics.\n";
        }
        StringBuilder sb = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> diag : finalDiagnostics) {
            sb.append("Kind: ").append(diag.getKind())
                    .append(", Message: ").append(diag.getMessage(null))
                    .append(", at: ").append(diag.getSource())
                    .append(":").append(diag.getLineNumber())
                    .append("\n");
        }
        return sb.toString();
    }
}
