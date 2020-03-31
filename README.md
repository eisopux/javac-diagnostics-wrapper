# javac diagnostics wrapper

Simple wrapper around javac to produce diagnostics output as JSON.

## How to Build

```shell
./gradlew shadowJar
```

will generate `build/libs/javac-diagnostics-wrapper-all.jar`.


## How to Use

Simply use instead of your usual javac command:

```shell
java \
    -jar /path/to/javac-diagnostics-wrapper-all.jar \
    -classpath /path/to/checker-framework/checker/dist/checker.jar \
    -processor org.checkerframework.checker.nullness.NullnessChecker \
    File1.java File2.java
```

will produce output like:

```
{"diagnostics":[{"fileUri":"file:///.../Dummy.java","kind":"ERROR","position":26,"startPosition":26,"endPosition":30,"lineNumber":2,"columnNumber":20,"code":"compiler.err.proc.messager","message":"[assignment.type.incompatible] incompatible types in assignment.\n  found   : null\n  required: @Initialized @NonNull Object"}]}
```

## How to Develop

To format the source code, run `$ ./gradlew spotlessApply`.

## Acknowledgements

- [checker-framework-language-server](https://github.com/eisopux/checker-framework-languageserver/)
