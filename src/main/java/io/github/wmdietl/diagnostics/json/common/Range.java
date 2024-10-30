package io.github.wmdietl.diagnostics.json.common;

/**
 * Range in a text document expressed as (zero-based) start and end positions. A range is
 * comparable to a selection in an editor. Therefore the end position is exclusive. If you want to
 * specify a range that contains a line including the line ending character(s) then use an end
 * position denoting the start of the next line.
 */
public class Range {
    /** The range's start position. */
    public Position start;

    /** The range's end position. */
    public Position end;

    /**
     * Create a new Range from javac standard diagnostic output
     * 
     * @param line see {@code diagnostic.getLineNumber()}
     * @param column see {@code diagnostic.getColumnNumber()}
     * @param startPos see {@code diagnostic.getStartPosition()}
     * @param endPos see {@code diagnostic.getEndPosition()}
     */
    public Range(final long line, final long column, final long startPos, final long endPos) {
        if(line < 1 || column < 1){
            this.start = this.end = Position.START;
            return;
        }
        this.start = new Position(line - 1, column - 1);
        this.end = new Position(line - 1, (column - 1 + endPos - startPos));
    }

    @Override
    public String toString() {
        return start + "-" + end;
    }

    /**
     * Define a position in a file using the x and y coordinates
     * y coordinates = line number
     * x coordinates = offset from the beginning of the line
     * 
     * The positions in a text document use zero-based line and zero-based character offset.
     * A position is between two characters like an "insert" cursor in a editor. Special values like for
     * example -1 to denote the end of a line are not supported.
     */
    private static class Position {
        /** Use this when you have no position information. */
        public static final Position START = new Position(0, 0);

        /** Line position in a document (zero-based). */
        public final long line;
        
        /**
         * Character offset on a line in a document (zero-based). Assuming that the line is represented
         * as a string, the `character` value represents the gap between the `character` and `character
         * + 1`.
         */
        public final long character;
        
        /**
         * Create a new Position.
         *
         * @param line line position in a document (zero-based)
         * @param character character offset on a line in a document (zero-based)
         */
        public Position(final long line, final long character) {
            this.line = line;
            this.character = character;
        }

        @Override
        public String toString() {
            return "(" + line + "," + character + ")";
        }
    }
}
