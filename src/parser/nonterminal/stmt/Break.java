package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;

public class Break extends ASDDefault implements Stmt {
    private final int line;

    public Break(int line) {
        this.line = line;
    }

    public int line() {
        return line;
    }
}
