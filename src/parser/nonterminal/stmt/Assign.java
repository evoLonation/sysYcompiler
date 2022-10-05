package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.exp.LVal;

public class Assign extends ASDDefault implements Stmt {
    private final LVal lVal;
    private final Exp exp;

    public Assign(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
        addSon(exp, lVal);
    }

    public LVal getLVal() {
        return lVal;
    }

    public Exp getExp() {
        return exp;
    }
}
