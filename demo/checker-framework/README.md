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
    -processor org.checkerframework.checker.nullness.NullnessChecker,org.checkerframework.checker.interning.InterningChecker \
    -Awarns \
    -AshowPrefixInWarningMessages \
    Demo.java InterningError.java OtherError.java
```

(`checker/dist/checker.jar` is the Checker Framework's all-in-one jar; the `io.github.eisop:checker`
Maven artifact used by the `checkerFrameworkDemo` Gradle task below works the same way.)

Three intentionally-flawed files, on purpose:

- `Demo.java`'s `myObject` is only assigned a non-null value conditionally, so the **Nullness
  Checker** flags the unconditional `myObject.toString()`.
- `InterningError.java` compares a `String` with `==` instead of `equals`, which the
  **Interning Checker** flags.
- `OtherError.java` has a plain type mismatch: a genuine javac error, unrelated to either checker.

All three must be separate files: a plain attribution error (`OtherError.java`'s) in a class stops
the Checker Framework from analyzing that same class, so combining it with either checker's bug
would silently lose that checker's finding. And running *two* independent top-level checkers
together via a comma-separated `-processor` list needs `-Awarns`: per the manual's own description
of `-processor`, "javac stops processing an indeterminate time after detecting an error. When
providing multiple checkers, if one checker detects any error, subsequent checkers may not run."
(Confirmed directly: without `-Awarns`, only the first-listed checker's finding showed up at all,
regardless of which checker was listed first or whether its bug was in the same file as the
other's.) `-Awarns` demotes checker errors to warnings, which does not trigger that stop.

`-AshowPrefixInWarningMessages` is also needed, for a different reason: many message keys are not
unique to one checker (e.g. `assignment.type.incompatible` is reported by several different type
systems), so a bare `ruleId` alone cannot always tell you which checker actually reported a given
result. This flag makes the Checker Framework prefix each message with `[checker:messageKey]`
instead of just `[messageKey]`; `SarifReporter` splits that prefix into the `ruleId` (still just
the message key, so it stays comparable across runs with or without the flag) and a `checker`
entry in the result's `properties` bag.

Running the command above under a JDK version the Checker Framework lists as tested (8, 11, 17, or
21) produces:

```json
{
  "version": "2.1.0",
  "runs": [
    {
      "tool": {
        "driver": {
          "name": "javac",
          "rules": [
            { "id": "dereference.of.nullable" },
            { "id": "not.interned" },
            { "id": "compiler.err.prob.found.req" }
          ]
        }
      },
      "results": [
        {
          "ruleId": "dereference.of.nullable",
          "level": "warning",
          "message": {
            "text": "dereference of possibly-null reference myObject"
          },
          "locations": [
            {
              "physicalLocation": {
                "artifactLocation": { "uri": "file:///.../demo/checker-framework/Demo.java" },
                "region": {
                  "startLine": 23,
                  "startColumn": 28,
                  "charOffset": 1020,
                  "charLength": 8
                }
              }
            }
          ],
          "properties": { "checker": "nullness" }
        },
        {
          "ruleId": "not.interned",
          "level": "warning",
          "message": {
            "text": "attempting to use a non-@Interned comparison operand"
          },
          "locations": [
            {
              "physicalLocation": {
                "artifactLocation": {
                  "uri": "file:///.../demo/checker-framework/InterningError.java"
                },
                "region": {
                  "startLine": 25,
                  "startColumn": 16,
                  "charOffset": 1475,
                  "charLength": 1
                }
              }
            }
          ],
          "properties": { "checker": "interning" }
        },
        {
          "ruleId": "compiler.err.prob.found.req",
          "level": "error",
          "message": {
            "text": "incompatible types: java.lang.String cannot be converted to int"
          },
          "locations": [
            {
              "physicalLocation": {
                "artifactLocation": { "uri": "file:///.../demo/checker-framework/OtherError.java" },
                "region": {
                  "startLine": 16,
                  "startColumn": 25,
                  "charOffset": 970,
                  "charLength": 14
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
(trimmed to the fields this project sets; copy-pasted from an actual run, not hand-written. Note
`level` is `"warning"` for the two checker findings because of `-Awarns`, but stays `"error"` for
the plain javac diagnostic, which `-Awarns` does not affect.)

Notice all three results have different `ruleId`s: `dereference.of.nullable` and `not.interned` --
each checker's own specific finding identifier -- and `compiler.err.prob.found.req`, javac's own
diagnostic code, for the plain type error. Without `SarifReporter`'s extraction, *every* Checker
Framework finding from *either* checker (there can be many, from many different checks) would
collapse onto the single generic `compiler.err.proc.messager`/`compiler.warn.proc.messager` code
javac uses for every annotation-processor-issued diagnostic, regardless of which checker or which
specific rule fired; `SarifReporter` extracts each one's `[messageKey]` prefix instead (see its
Javadoc), specifically so that results from different checks -- and different checkers -- are
distinguishable by `ruleId`, which is what makes a per-rule baseline comparison possible across
more than one checker in the first place.

Also notice the two checker findings each carry a `"properties": { "checker": ... }`, but the
plain javac finding does not. A `ruleId` by itself is not always enough to tell checkers apart:
this demo's two message keys happen to be unique to their checker, but many message keys are
reused across type systems (`assignment.type.incompatible`, for instance, is reported by several
different checkers), so a consumer that groups or filters results by `ruleId` alone could conflate
findings from two unrelated checkers. The `checker` property resolves that without having to
re-parse the message text; it is only present when `-AshowPrefixInWarningMessages` gave
`SarifReporter` a checker name to extract in the first place.

## Running it via Gradle

```shell
./gradlew checkerFrameworkDemo
```

builds this exact scenario using `io.github.eisop:checker` from Maven Central (version pinned in
`build.gradle`) and writes the SARIF output to `build/demo/checker-framework-nullness.sarif.json`.
This task is wired into `check`, so it also runs as part of `./gradlew build`, as a regression
test that the wrapper and the Checker Framework still work together as shown above.
