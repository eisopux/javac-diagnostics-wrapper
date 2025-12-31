package io.github.eisopux.diagnostics.core;

import java.io.File;
import java.util.*;

import javax.lang.model.SourceVersion;
import javax.tools.*;

/**
 * CompilationTaskBuilder is a utility class that encapsulates the creation of a {@link
 * javax.tools.JavaCompiler.CompilationTask} for a {@link Collector} to attach to.
 */
public class CompilationTaskBuilder {
    private final JavaCompiler compiler;
    private final StandardJavaFileManager fileManager;
    private final JavacOptions options;
    private javax.tools.DiagnosticCollector<JavaFileObject> diagnosticListener;
    private final Iterable<? extends JavaFileObject> javaFiles;

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

    /**
     * Creates a new CompilationTaskBuilder instance by parsing command-line arguments. This method
     * retrieves the system Java compiler, obtains its standard file manager, and uses {@link
     * JavacOptions#parse(OptionChecker, OptionChecker, String...)} to separate the arguments into
     * recognized options, class names, and source file paths.
     *
     * @param args the command-line arguments to be parsed and used for the compilation task
     * @return a configured CompilationTaskBuilder ready to build a CompilationTask
     * @throws IllegalStateException if no system Java compiler is found
     */
    public static CompilationTaskBuilder fromArgs(String[] args) {
        JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException(
                    "No system Java compiler found. Are you running a JRE instead of a JDK?");
        }
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        JavacOptions options = JavacOptions.parse(compiler, fileManager, args);
        if (!options.getUnrecognizedOptions().isEmpty()) {
            System.err.println("Invalid options: " + options);
            System.exit(1);
        }

        Iterable<? extends JavaFileObject> javaFiles =
                fileManager.getJavaFileObjectsFromFiles(options.getFiles());

        return new CompilationTaskBuilder(compiler, fileManager, options, null, javaFiles);
    }

    /**
     * Returns this builder’s diagnostic collector. If none exists yet, the method lazily creates
     * one and reuses it on subsequent calls.
     *
     * @return the diagnostic collector instance
     */
    public DiagnosticCollector<JavaFileObject> getOrCreateDiagnosticListener() {
        if (this.diagnosticListener == null) {
            this.diagnosticListener = new DiagnosticCollector<>();
        }
        return this.diagnosticListener;
    }

    /**
     * Builds a fully configured {@link javax.tools.JavaCompiler.CompilationTask}.
     *
     * @return a CompilationTask ready to be executed
     */
    public JavaCompiler.CompilationTask build() {
        return compiler.getTask(
                null,
                fileManager,
                diagnosticListener,
                options.getRecognizedOptions(),
                options.getClassNames(),
                javaFiles);
    }

    /** JavacOptions encapsulates the parsing of command-line arguments for the Java compiler. */
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

        static JavacOptions parse(
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

        List<String> getRecognizedOptions() {
            return Collections.unmodifiableList(recognizedOptions);
        }

        List<File> getFiles() {
            return Collections.unmodifiableList(files);
        }

        List<String> getClassNames() {
            return Collections.unmodifiableList(classNames);
        }

        List<String> getUnrecognizedOptions() {
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
