package parser.nonterminal.stmt;

import parser.nonterminal.ASTDefault;

public class Continue extends ASTDefault implements Stmt {
    private final int line;

    public Continue(int line) {
        this.line = line;
    }

    public int line() {
        return line;
    }
}
