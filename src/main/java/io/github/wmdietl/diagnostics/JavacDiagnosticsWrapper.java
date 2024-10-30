package io.github.wmdietl.diagnostics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.OptionChecker;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Wrapper around javac to output diagnostics in an easily-configurable way.
 *
 * <p>Based on code examples from: http://openjdk.java.net/groups/compiler/guide/compilerAPI.html
 */
public abstract class JavacDiagnosticsWrapper {

    public void run(String[] args) {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = javac.getStandardFileManager(null, null, null);
        JavacOptions options = JavacOptions.parse(javac, fileManager, args);

        if (!options.getUnrecognizedOptions().isEmpty()) {
            System.err.println("Invalid options: " + options);
            System.exit(1);
        }

        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        Iterable<? extends JavaFileObject> javaFiles =
                fileManager.getJavaFileObjectsFromFiles(options.getFiles());

        javac.getTask(
                        null,
                        fileManager,
                        diagnosticCollector,
                        options.getRecognizedOptions(),
                        options.getClassNames(),
                        javaFiles)
                .call();

        processDiagnostics(diagnosticCollector.getDiagnostics());
    }

    /**
     * Callback to handle the diagnostics from a compilation task. At the moment this outputs a JSON
     * message. In the future, maybe extend to have multiple subclasses for different formats.
     */
    protected abstract void processDiagnostics(List<Diagnostic<? extends JavaFileObject>> diagnostics);

    /**
     * Decode Java compiler options.
     *
     * @author Peter von der Ahe
     */
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
            List<String> recognizedOptions = new ArrayList<String>();
            List<String> unrecognizedOptions = new ArrayList<String>();
            List<String> classNames = new ArrayList<String>();
            List<File> files = new ArrayList<File>();
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

        /**
         * Returns the list of recognized options and their arguments.
         *
         * @return a list of options
         */
        public List<String> getRecognizedOptions() {
            return Collections.unmodifiableList(recognizedOptions);
        }

        /**
         * Returns the list of file names.
         *
         * @return a list of file names
         */
        public List<File> getFiles() {
            return Collections.unmodifiableList(files);
        }

        /**
         * Returns the list of class names.
         *
         * @return a list of class names
         */
        public List<String> getClassNames() {
            return Collections.unmodifiableList(classNames);
        }

        /**
         * Returns the list of unrecognized options.
         *
         * @return a list of unrecognized options
         */
        public List<String> getUnrecognizedOptions() {
            return Collections.unmodifiableList(unrecognizedOptions);
        }

        @Override
        public String toString() {
            return String.format(
                    "recognizedOptions = %s; classNames = %s; "
                            + "files = %s; unrecognizedOptions = %s",
                    recognizedOptions, classNames, files, unrecognizedOptions);
        }
    }
}
