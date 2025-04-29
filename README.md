# javac diagnostics wrapper

A customizable wrapper around javac that aggregates and outputs compiler diagnostics in a desired
format using a pluggable architecture.

The `javac diagnostics wrapper` is part of the larger https://eisopux.github.io/ project.

## How to Build

```shell
./gradlew assemble
```

will generate `build/libs/javac-diagnostics-wrapper-all.jar`.


## How to Use

Simply use instead of your usual `javac` command.

Instead of

```shell
javac [flags] File1.java File2.java
```
(where `[flags]` is a placeholder for 0 or more actual javac flags you're using)

use

```shell
java \
    -cp /path/to/javac-diagnostics-wrapper-all.jar \
    io.github.eisopux.diagnostics.builtin.JsonDiagnostics \
    [flags] File1.java File2.java
```
(where `[flags]` is a placeholder for 0 or more actual javac flags you're using)

There are currently two builtin outputs using a Diagnostics Collector:
- `.io.github.eisopux.diagnostics.builtin.LspDiagnostics` produces output in the [LSP JSON format](https://microsoft.github.io/language-server-protocol/specification).
- `io.github.eisopux.diagnostics.builtin.JsonDiagnostics` produces output in a JSON format
   directly corresponding to the javac diagnostics.


## Examples

Normal compilation of a file with errors, using the javac format:

```shell
java \
    -cp /path/to/javac-diagnostics-wrapper-all.jar \
    io.github.eisopux.diagnostics.builtin.JsonDiagnostics \
    File1.java
```

results in

```
{
  "diagnostics": [
    {
      "source": "file:///.../File1.java",
      "kind": "ERROR",
      "position": 29,
      "startPosition": 29,
      "endPosition": 30,
      "lineNumber": 2,
      "columnNumber": 16,
      "code": "compiler.err.prob.found.req",
      "message": "incompatible types: int cannot be converted to java.lang.String"
    },
    {
      "source": "file:///.../File1.java",
      "kind": "ERROR",
      "position": 64,
      "startPosition": 64,
      "endPosition": 69,
      "lineNumber": 4,
      "columnNumber": 16,
      "code": "compiler.err.prob.found.req",
      "message": "incompatible types: unexpected return value"
    }
  ]
}
```

Compilation of a file using the [Checker Framework](https://www.checkerframework.org/),
using the LSP format:

```shell
java \
    -cp /path/to/javac-diagnostics-wrapper-all.jar \
    io.github.eisopux.diagnostics.builtin.LspDiagnostics \
    -classpath /path/to/checker-framework/checker/dist/checker.jar \
    -processor org.checkerframework.checker.nullness.NullnessChecker \
    -AshowPrefixInWarningMessages
    File2.java
```

results in:

```
[
  {
    "uri": "file://.../File2.java",
    "diagnostics": [
      {
        "range": {
          "start": {
            "line": 12,
            "character": 14
          },
          "end": {
            "line": 12,
            "character": 16
          }
        },
        "severity": 1,
        "code": "compiler.err.proc.messager",
        "source": "javac",
        "message": "[argument.type.incompatible] incompatible types in argument.\n  found   : @Initialized @Nullable InputStream\n  required: @Initialized @NonNull InputStream"
      }
    ]
  }
]
```
Note that the `-AshowPrefixInWarningMessages` is an optional Checker Framework flag
and will attach correct processor information to formats that support this information.

## How to Develop

To format the source code, run `./gradlew spotlessApply`.

### Architecture Overview

The **javac Diagnostics Wrapper** features a modular, pluggable design that 
decouples data collection from output formatting. It utilizes a collector-reporter interface
that allows developers to easily implement custom diagnostic gathering and presentation
formats.


#### Collectors

Components that hook into the javac compilation process to gather desired information. 
Each collector implements the `Collector` interface and contributes its collected data as
a list of key/value pairs to a centralized `CompilationReportData` instance.

#### Reporters

Components that process the aggregated `CompilationReportData` and generate output in various formats 
(e.g., console text, JSON, or LSP diagnostics). Each reporter implements the `Reporter` interface and is responsible for 
formatting the data according to its output standard.


### Extending the System

#### Implement the Collector Interface

Create a new class that implements `io.github.eisopux.diagnostics.core.Collector`. Override:
- `onBeforeCompile(CompilationTaskBuilder builder)` if you need to attach listeners or initialize data structures.
- `onAfterCompile(CompilationReportData reportData)` to finalize your data and populate a report section as a list of key/value pairs.


#### Implement the Reporter Interface

Create a new class that implements `io.github.eisopux.diagnostics.core.Reporter` and its 
`generateReport(CompilationReportData reportData)` method. Format the data according 
to your output requirements.

#### Create An Output Configuration

The `javac-diagnositc-wrapper` produces output by combining one or more `Collectors` with exactly one
`Reporter`. To create an easy-to-use output configuration, add a new 
class to the`io.github.eisopux.diagnostics.builtin` package. This class will encapsulate 
the desired collectors and reporter, providing a convenient entry
point for generating an output. An example implementation is shown below:

```java
public class JsonDiagnostics {
    public static void main(String[] args) {
        CompilerRunner runner =
                new CompilerRunner()
                        .addCollector(new DiagnosticCollector())
                        .setReporter(new JSONReporter());

        runner.run(args);
    }
}
```

Where `.addCollector` should be called one or more times to combine
multiple collectors and `.setReporter` should be called exactly once to select 
the desired output format.

## Roadmap

- [ ] SARIF Output
- [ ] Build tool integration
- [ ] Improved CLI interface
- [ ] Performance Collector
- [ ] Support processor information outside of Checker Framework package


## Acknowledgements

- [Compiler API guide](http://openjdk.java.net/groups/compiler/guide/compilerAPI.html)
- [Checker Framework language server](https://github.com/eisopux/checker-framework-languageserver/)
