package org.eisopux.diagnostics.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.tools.*;

/** A runner that sets up the javac task, attaches collectors, and runs the compilation. */
public class CompilerRunner {

    private final List<Collector<?>> collectors = new ArrayList<>();

    public CompilerRunner addCollector(Collector<?> collector) {
        this.collectors.add(collector);
        return this;
    }

    public CompilerRunner setReporter(Reporter reporter) {
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
        collectors.forEach(Collector::onBeforeCompile);

        // 4. Prepare the compilation input
        Iterable<? extends JavaFileObject> javaFiles =
                fileManager.getJavaFileObjectsFromFiles(options.getFiles());

        // 5. Create the compilation task
        JavaCompiler.CompilationTask task =
                javac.getTask(
                        null, // default Writer (System.err)
                        fileManager,
                        null, // We'll let collectors attach themselves
                        options.getRecognizedOptions(),
                        options.getClassNames(),
                        javaFiles);

        // 6. Register task-aware collectors
        //        for (JavacCollector collector : collectors) {
        //            if (collector instanceof TaskAwareCollector) {
        //                ((TaskAwareCollector) collector).registerWithTask(task);
        //            }
        //        }

        collectors.forEach(collector -> collector.attachToTask(task));

        // 8. Call the compilation task
        boolean success = task.call();

        // 9. Call onAfterCompile(success) for each collector
        collectors.forEach(c -> c.onAfterCompile(success));
    }

    private static final class JavacOptions {
        private final List<String> recognizedOptions;
        private final List<String> classNames;
        private final List<File> files;
        private final List<String> unrecognizedOptions;

        private JavacOptions(
                List<String> recognizedOptions,
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

            return new JavacOptions(recognizedOptions, classNames, files, unrecognizedOptions);
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
                    recognizedOptions, classNames, files, unrecognizedOptions);
        }
    }
}
