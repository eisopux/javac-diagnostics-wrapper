package org.eisopux.diagnostics.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.tools.*;

public class CompilationTaskBuilder {
    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private JavacOptions options;
    private javax.tools.DiagnosticCollector<JavaFileObject> diagnosticListener;
    private Iterable<? extends JavaFileObject> javaFiles;

    private CompilationTaskBuilder(
            JavaCompiler compiler,
            StandardJavaFileManager fileManager,
            JavacOptions options,
            DiagnosticCollector<JavaFileObject> diagnosticListener,
            Iterable<? extends JavaFileObject> javaFiles) {
        this.compiler = compiler;
        this.fileManager = fileManager;
        this.options = options;
        this.diagnosticListener = diagnosticListener;
        this.javaFiles = javaFiles;
    }

    public static CompilationTaskBuilder fromArgs(String[] args) {
        // 1. Get the system compiler and file manager.
        JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException(
                    "No system Java compiler found. Are you running a JRE instead of a JDK?");
        }
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // 2. Parse arguments.
        JavacOptions options = JavacOptions.parse(compiler, fileManager, args);
        if (!options.getUnrecognizedOptions().isEmpty()) {
            System.err.println("Invalid options: " + options);
            System.exit(1);
        }

        Iterable<? extends JavaFileObject> javaFiles =
                fileManager.getJavaFileObjectsFromFiles(options.getFiles());

        return new CompilationTaskBuilder(compiler, fileManager, options, null, javaFiles);
    }

    public void addDiagnosticListener(DiagnosticCollector<JavaFileObject> listener) {
        this.diagnosticListener = listener;
    }

    public JavaCompiler.CompilationTask build() {
        return compiler.getTask(
                null,
                fileManager,
                diagnosticListener,
                options.getRecognizedOptions(),
                options.getClassNames(),
                javaFiles);
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
