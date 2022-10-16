package parser.nonterminal.stmt;

import parser.nonterminal.ASTDefault;

public class Break extends ASTDefault implements Stmt {
    private final int line;

    public Break(int line) {
        this.line = line;
    }

    public int line() {
        return line;
    }
}
