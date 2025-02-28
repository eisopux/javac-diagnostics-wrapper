package org.eisopux.diagnostics.core;

import org.eisopux.diagnostics.collectors.DiagnosticCollectorImpl;

import javax.lang.model.SourceVersion;
import javax.tools.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A runner that sets up the javac task, attaches collectors, and runs the compilation.
 */
public class CompilerRunner {

    private final List<JavacCollector> collectors = new ArrayList<>();
    private FormatReporter reporter;


    public CompilerRunner addCollector(JavacCollector collector) {
        collectors.add(collector);
        return this;
    }

    public CompilerRunner setReporter(FormatReporter reporter) {
        this.reporter = reporter;
        return this;
    }

    public void run(String[] args) {
        // 1. Get the system compiler and file manager
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if (javac == null) {
            throw new IllegalStateException(
                    "No system Java compiler found. Are you running a JRE instead of a JDK?");
        }
        StandardJavaFileManager fileManager = javac.getStandardFileManager(null, null, null);

        // 2. Parse arguments (replicating the logic from original JavacDiagnosticsWrapper)
        JavacOptions options = JavacOptions.parse(javac, fileManager, args);

        if (!options.getUnrecognizedOptions().isEmpty()) {
            System.err.println("Invalid options: " + options);
            System.exit(1);
        }

        // 3. Call onBeforeCompile() for each collector
        collectors.forEach(JavacCollector::onBeforeCompile);

        // 4. Find the DiagnosticCollectorImpl, if present, so we can pass its underlying collector to javac
        DiagnosticCollector<JavaFileObject> diagCollector = findDiagnosticCollector();

        // 5. Prepare the compilation input
        Iterable<? extends JavaFileObject> javaFiles =
                fileManager.getJavaFileObjectsFromFiles(options.getFiles());

        // 6. Create the compilation task
        JavaCompiler.CompilationTask task = javac.getTask(
                null, // default Writer (System.err)
                fileManager,
                diagCollector, // pass the collector if found, otherwise null
                options.getRecognizedOptions(),
                options.getClassNames(),
                javaFiles
        );

        // 7. Register task-aware collectors
        for (JavacCollector collector : collectors) {
            if (collector instanceof TaskAwareCollector) {
                ((TaskAwareCollector) collector).registerWithTask(task);
            }
        }

        // 8. Call the compilation task
        boolean success = task.call();

        // 9. Call onAfterCompile(success) for each collector
        collectors.forEach(c -> c.onAfterCompile(success));

        // 10. Print the collected diagnostics to console
        if (reporter != null) {
            String resultOutput = reporter.generateReport(collectors, success);
            System.out.println(resultOutput);
        } else {
            System.out.println("No reporter found");
        }
    }

    /**
     * Look for a DiagnosticCollectorImpl among our registered collectors.
     * Return null if none is found.
     */
    private DiagnosticCollector<JavaFileObject> findDiagnosticCollector() {
        DiagnosticCollectorImpl impl = findCollector(DiagnosticCollectorImpl.class);
        return impl == null ? null : impl.getUnderlyingCollector();
    }

    /**
     * Utility to find a collector of a given type among our registered collectors.
     */
    private <T extends JavacCollector> T findCollector(Class<T> type) {
        for (JavacCollector c : collectors) {
            if (type.isInstance(c)) {
                return type.cast(c);
            }
        }
        return null;
    }

    private static final class JavacOptions {
        private final List<String> recognizedOptions;
        private final List<String> classNames;
        private final List<File> files;
        private final List<String> unrecognizedOptions;

        private JavacOptions(List<String> recognizedOptions,
                             List<String> classNames,
                             List<File> files,
                             List<String> unrecognizedOptions) {
            this.recognizedOptions = recognizedOptions;
            this.classNames = classNames;
            this.files = files;
            this.unrecognizedOptions = unrecognizedOptions;
        }

        public static JavacOptions parse(
                OptionChecker primary, OptionChecker secondary, String... arguments) {

            List<String> recognizedOptions = new ArrayList<>();
            List<String> unrecognizedOptions = new ArrayList<>();
            List<String> classNames = new ArrayList<>();
            List<File> files = new ArrayList<>();

            for (int i = 0; i < arguments.length; i++) {
                String argument = arguments[i];
                int optionCount = primary.isSupportedOption(argument);
                if (optionCount < 0) {
                    optionCount = secondary.isSupportedOption(argument);
                }
                if (optionCount < 0) {
                    File file = new File(argument);
                    if (file.exists()) {
                        files.add(file);
                    } else if (SourceVersion.isName(argument)) {
                        classNames.add(argument);
                    } else {
                        unrecognizedOptions.add(argument);
                    }
                } else {
                    for (int j = 0; j < optionCount + 1; j++) {
                        int index = i + j;
                        if (index == arguments.length) {
                            throw new IllegalArgumentException(argument);
                        }
                        recognizedOptions.add(arguments[index]);
                    }
                    i += optionCount;
                }
            }

            return new JavacOptions(
                    recognizedOptions, classNames, files, unrecognizedOptions
            );
        }

        public List<String> getRecognizedOptions() {
            return Collections.unmodifiableList(recognizedOptions);
        }

        public List<File> getFiles() {
            return Collections.unmodifiableList(files);
        }

        public List<String> getClassNames() {
            return Collections.unmodifiableList(classNames);
        }

        public List<String> getUnrecognizedOptions() {
            return Collections.unmodifiableList(unrecognizedOptions);
        }

        @Override
        public String toString() {
            return String.format(
                    "recognizedOptions = %s; classNames = %s; files = %s; unrecognizedOptions = %s",
                    recognizedOptions, classNames, files, unrecognizedOptions
            );
        }
    }
}
