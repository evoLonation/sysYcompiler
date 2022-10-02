package parser.nonterminal.stmt;

import parser.nonterminal.exp.Exp;

public class Return implements Stmt {
    private Exp exp;

    public Return(Exp exp) {
        this.exp = exp;
    }
}
