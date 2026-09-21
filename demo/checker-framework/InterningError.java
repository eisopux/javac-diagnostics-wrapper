/**
 * A third fixture for the {@code checkerFrameworkDemo} Gradle task, compiled alongside {@code
 * Demo.java} and {@code OtherError.java}: the Interning Checker reports {@code not.interned} on
 * {@code s == "foo"}, since {@code s} is not known to be {@code @Interned}. This, together with
 * {@code Demo.java}'s Nullness Checker finding, shows two <em>different checkers'</em> findings
 * getting two different, correctly-distinguished SARIF {@code ruleId}s in the same run -- not just
 * two different rules from the same checker.
 *
 * <p>Running two independent top-level checkers together this way needs {@code -Awarns}: per the
 * Checker Framework manual's own description of {@code -processor}, "javac stops processing an
 * indeterminate time after detecting an error. When providing multiple checkers, if one checker
 * detects any error, subsequent checkers may not run." {@code -Awarns} demotes checker errors to
 * warnings, which does not trigger that stop (confirmed by running both checkers together with and
 * without it).
 */
public class InterningError {
    /**
     * Compares {@code s} to a string literal with {@code ==} instead of {@code equals}, which the
     * Interning Checker flags unless {@code s} is known to be {@code @Interned}.
     *
     * @param s a string that is not known to be interned
     * @return whether {@code s} is (reference-)equal to the literal {@code "foo"}
     */
    static boolean isFoo(String s) {
        return s == "foo";
    }
}
