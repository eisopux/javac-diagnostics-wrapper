/**
 * A trivial, always-compiles-cleanly fixture for the {@code smokeTest} Gradle task, which runs
 * the packaged application against this file to confirm it actually starts up and exits 0 --
 * something {@code ./gradlew build} does not otherwise check (see build.gradle).
 */
public class Smoke {
    public static void main(String[] args) {
        System.out.println("smoke test ok");
    }
}
