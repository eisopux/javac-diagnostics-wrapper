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

There are currently three builtin outputs using a Diagnostics Collector:
- `io.github.eisopux.diagnostics.builtin.LspDiagnostics` produces output in the [LSP JSON format](https://microsoft.github.io/language-server-protocol/specification).
- `io.github.eisopux.diagnostics.builtin.JsonDiagnostics` produces output in a JSON format
   directly corresponding to the javac diagnostics.
- `io.github.eisopux.diagnostics.builtin.SarifDiagnostics` produces output in the
  [SARIF 2.1.0 format](https://sarifweb.azurewebsites.net). **Experimental**: see
  `SarifReporter`'s class Javadoc for why (eisopux/javac-diagnostics-wrapper#152).


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

Compilation of a file with an error, using the SARIF format:

```shell
java \
    -cp /path/to/javac-diagnostics-wrapper-all.jar \
    io.github.eisopux.diagnostics.builtin.SarifDiagnostics \
    File1.java
```

results in

```
{
  "version": "2.1.0",
  "runs": [
    {
      "tool": {
        "driver": {
          "name": "javac",
          "rules": [
            {
              "id": "compiler.err.prob.found.req"
            }
          ]
        }
      },
      "results": [
        {
          "ruleId": "compiler.err.prob.found.req",
          "level": "error",
          "message": {
            "text": "incompatible types: java.lang.String cannot be converted to int"
          },
          "locations": [
            {
              "physicalLocation": {
                "artifactLocation": {
                  "uri": "file:///.../File1.java"
                },
                "region": {
                  "startLine": 3,
                  "startColumn": 17,
                  "charOffset": 50,
                  "charLength": 12
                }
              }
            }
          ]
        }
      ]
    }
  ]
}
```
(trimmed here to the fields this project sets; the actual output additionally carries a handful
of schema-default fields such as `driver.language`/`run.newlineSequences` that the SARIF library
fills in on its own.)

## How to Develop

To format the source code, run `./gradlew spotlessApply`.

### Architecture Overview

The **javac diagnostics wrapper** features a modular, pluggable design that 
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
                        .setReporter(new JsonReporter());

        System.exit(runner.run(args) ? 0 : 1);
    }
}
```

Where `.addCollector` should be called one or more times to combine
multiple collectors and `.setReporter` should be called exactly once to select 
the desired output format.

## Demos

- [`demo/checker-framework/`](demo/checker-framework/README.md): using this wrapper together with
  the [EISOP Checker Framework](https://eisop.github.io/) to get a SARIF baseline of a checker's
  findings on an existing codebase.

## Releasing

This project publishes to Maven Central under the `io.github.eisopux` groupId, using plain
Gradle `maven-publish`/`signing` (no third-party publishing plugin), the same approach
[eisop/checker-framework uses](https://github.com/eisop/checker-framework/blob/master/docs/developer/maven-central-publishing.md)
-- see that doc for the general explanation of why a Central Publishing Portal plugin isn't used,
and for background on the publish/staging/signing flow.

One-time maintainer setup, before the first release:

- Publish rights on the `io.github.eisopux` namespace in the
  [Central Portal](https://central.sonatype.com/). This is a separate namespace from
  checker-framework's own `io.github.eisop`, verified against the `eisopux` GitHub org, and
  needs its own verification even if you already hold `io.github.eisop`.
- A GPG key on a public keyserver, and `signing.gnupg.keyName` set to it (in
  `~/.gradle/gradle.properties` or via `-Psigning.gnupg.keyName=...`).
- A Central Portal user token, set as `SONATYPE_NEXUS_USERNAME`/`SONATYPE_NEXUS_PASSWORD` Gradle
  properties.

To publish a release:

```shell
./gradlew publish -Prelease --no-parallel
```

then log in to the [Central Portal](https://central.sonatype.com/publishing/deployments) and
manually publish the staged deployment (this project has not automated that last click; see the
checker-framework doc above for how it could be).

Note that `com.jetbrains.qodana:qodana-sarif` (see `SarifReporter`'s class Javadoc) is not on
Maven Central, so a consumer of this project's published artifact who uses `SarifDiagnostics`
also needs the same custom repository declaration this project's own `build.gradle` uses.

## Acknowledgements

- [Compiler API guide](http://openjdk.java.net/groups/compiler/guide/compilerAPI.html)
- [Checker Framework language server](https://github.com/eisopux/checker-framework-languageserver/)
