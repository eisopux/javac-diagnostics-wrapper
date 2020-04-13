# javac diagnostics wrapper

Wrapper around javac to output diagnostics in an easily-configurable way.

## How to Build

```shell
./gradlew assemble
```

will generate `build/libs/javac-diagnostics-wrapper-all.jar`.


## How to Use

Simply use instead of your usual `javac` command.

Instead of

```shell
javac -flags File1.java File2.java
```

use

```shell
java \
    -cp /path/to/javac-diagnostics-wrapper-all.jar \
    io.github.wmdietl.diagnostics.json.lsp.Main \
    -flags File1.java File2.java
```

There are currently two supported output formats:
- `io.github.wmdietl.diagnostics.json.lsp.Main` produces output in the [LSP JSON format](https://microsoft.github.io/language-server-protocol/specification).
- `io.github.wmdietl.diagnostics.json.javac.Main` produces output in a JSON format
   directly corresponding to the javac diagnostics.


## Examples

Normal compilation of a file with errors, using the javac format:

```shell
java \
    -cp /path/to/javac-diagnostics-wrapper-all.jar \
    io.github.wmdietl.diagnostics.json.javac.Main \
    File1.java
```

results in

```
{
  "diagnostics": [
    {
      "fileUri": "file:///.../File1.java",
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
      "fileUri": "file:///.../File1.java",
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
    io.github.wmdietl.diagnostics.json.lsp.Main \
    -classpath /path/to/checker-framework/checker/dist/checker.jar \
    -processor org.checkerframework.checker.nullness.NullnessChecker \
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
        "source": "Main",
        "message": "[argument.type.incompatible] incompatible types in argument.\n  found   : @Initialized @Nullable InputStream\n  required: @Initialized @NonNull InputStream"
      }
    ]
  }
]
```


## How to Develop

To format the source code, run `./gradlew spotlessApply`.


## Acknowledgements

- [Compiler API guide](http://openjdk.java.net/groups/compiler/guide/compilerAPI.html)
- [Checker Framework language server](https://github.com/eisopux/checker-framework-languageserver/)
