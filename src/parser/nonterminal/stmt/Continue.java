package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;

public class Continue extends ASDDefault implements Stmt {
    private final int line;

    public Continue(int line) {
        this.line = line;
    }

    public int line() {
        return line;
    }
}
