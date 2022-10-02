package parser.nonterminal.stmt;

import parser.nonterminal.exp.Exp;

public class While implements Stmt {
    private Exp cond;
    private Stmt stmt;

    public While(Exp cond, Stmt stmt) {
        this.cond = cond;
        this.stmt = stmt;
    }
}
