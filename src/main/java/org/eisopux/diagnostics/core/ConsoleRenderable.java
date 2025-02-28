package org.eisopux.diagnostics.core;

/**
 * Optional interface that collectors can implement if they want
 * to provide a simple console representation of their data.
 */
public interface ConsoleRenderable {
    /**
     * Returns a string that should be included in the console output,
     * e.g., listing diagnostics or processor details.
     */
    String toConsoleString();
}
