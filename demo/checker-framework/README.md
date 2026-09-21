# Demo: Checker Framework + SARIF as a baseline

This directory demonstrates using `javac-diagnostics-wrapper` together with the
[EISOP Checker Framework](https://eisop.github.io/) to get a structured, tool-independent snapshot
of a checker's findings -- a starting point for a "baseline": adopting a checker on an existing
codebase usually means it reports far more findings than you can fix immediately, so a common
approach is to record the current findings once, commit that snapshot, and only fail CI on *new*
findings from then on. SARIF's `ruleId` and `locations` per result are exactly the structure such
a baseline comparison needs.

This wrapper project has **no dependency on the Checker Framework** anywhere in its main build
(see the `checkerFrameworkDemo` configuration in `build.gradle`, which exists solely to run this
demo and is never part of the packaged `javac-diagnostics-wrapper-all.jar`). Combining the two is
something *you* do at the command line, the same way this demo does.

## Running it yourself

```shell
java \
    -cp /path/to/javac-diagnostics-wrapper-all.jar \
    io.github.eisopux.diagnostics.builtin.SarifDiagnostics \
    -classpath /path/to/checker-framework/checker/dist/checker.jar \
    -processor org.checkerframework.checker.nullness.NullnessChecker \
    Demo.java
```

(`checker/dist/checker.jar` is the Checker Framework's all-in-one jar; the `io.github.eisop:checker`
Maven artifact used by the `checkerFrameworkDemo` Gradle task below works the same way.)

`Demo.java` has one intentional bug: `myObject` is only assigned a non-null value conditionally,
so the Nullness Checker flags the unconditional `myObject.toString()`. Running the command above
under a JDK version the Checker Framework lists as tested (8, 11, 17, or 21) produces:

```json
{
  "version": "2.1.0",
  "runs": [
    {
      "tool": {
        "driver": {
          "name": "javac",
          "rules": [
            { "id": "dereference.of.nullable" }
          ]
        }
      },
      "results": [
        {
          "ruleId": "dereference.of.nullable",
          "level": "error",
          "message": {
            "text": "dereference of possibly-null reference myObject"
          },
          "locations": [
            {
              "physicalLocation": {
                "artifactLocation": {
                  "uri": "file:///.../demo/checker-framework/Demo.java"
                },
                "region": {
                  "startLine": 13,
                  "startColumn": 28,
                  "charOffset": 475,
                  "charLength": 8
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
(trimmed to the fields this project sets; copy-pasted from an actual run, not hand-written. On
any other JDK version, the Checker Framework additionally emits a `compiler.note.proc.messager`
finding warning that the JDK is untested -- harmless, but it will show up as an extra result.)

Notice `ruleId` is `dereference.of.nullable` -- the Checker Framework's own specific finding
identifier -- not the generic `compiler.err.proc.messager` code javac itself reports for every
annotation-processor-issued diagnostic regardless of which checker or rule fired.
`SarifReporter` extracts it from the `[messageKey]` prefix every Checker Framework message
carries (see `SarifReporter`'s Javadoc), specifically so that results from different checks are
distinguishable by `ruleId` -- which is what makes a per-rule baseline comparison possible in the
first place.

## Running it via Gradle

```shell
./gradlew checkerFrameworkDemo
```

builds this exact scenario using `io.github.eisop:checker` from Maven Central (version pinned in
`build.gradle`) and writes the SARIF output to `build/demo/checker-framework-nullness.sarif.json`.
This task is wired into `check`, so it also runs as part of `./gradlew build`, as a regression
test that the wrapper and the Checker Framework still work together as shown above.
