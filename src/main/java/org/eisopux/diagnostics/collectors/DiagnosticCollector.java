package org.eisopux.diagnostics.collectors;

import org.eisopux.diagnostics.core.Collector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

/**
 * A concrete implementation of {@link Collector} that wraps a {@link
 * javax.tools.DiagnosticCollector} to capture compilation diagnostics.
 */
public class DiagnosticCollector implements Collector<Diagnostic<? extends JavaFileObject>> {

    private final javax.tools.DiagnosticCollector<JavaFileObject> diagCollector =
            new javax.tools.DiagnosticCollector<>();

    private List<Diagnostic<? extends JavaFileObject>> finalDiagnostics;

    @Override
    public void onBeforeCompile() {
        // no-op
    }

    @Override
    public void attachToTask(JavaCompiler.CompilationTask task) {
        try {
            Field contextField = task.getClass().getSuperclass().getDeclaredField("context");
            contextField.setAccessible(true);
            Object context = contextField.get(task);

            Method putMethod = context.getClass().getMethod("put", Class.class, Object.class);
            putMethod.invoke(context, javax.tools.DiagnosticListener.class, this.diagCollector);

            System.out.println("Successfully attached diagnostic listener via context.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Failed to attach diagnostic listener via reflection using context", e);
        }
    }

    @Override
    public void onAfterCompile(boolean success) {
        // Gather final diagnostics
        finalDiagnostics = diagCollector.getDiagnostics();
    }

    @Override
    public List<Diagnostic<? extends JavaFileObject>> getItems() {
        return finalDiagnostics;
    }
}
