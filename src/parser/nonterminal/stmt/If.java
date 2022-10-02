package parser.nonterminal.stmt;

import parser.nonterminal.exp.Exp;

public class If implements Stmt {
    private Exp cond;
    private Stmt ifStmt;
    private Stmt elseStmt;

    public If(Exp cond, Stmt ifStmt, Stmt elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
    }
}

