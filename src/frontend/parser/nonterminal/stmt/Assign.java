package frontend.parser.nonterminal.stmt;

import frontend.parser.nonterminal.exp.Exp;
import frontend.parser.nonterminal.exp.LVal;

public class Assign implements Stmt {
    private final LVal lVal;
    private final Exp exp;

    public Assign(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
    }

    public LVal getLVal() {
        return lVal;
    }

    public Exp getExp() {
        return exp;
    }
}
