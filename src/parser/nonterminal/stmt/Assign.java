package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.exp.LVal;

public class Assign extends ASDDefault implements Stmt {
    private LVal lVal;
    private Exp exp;

    public Assign(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
        addSon(exp);
    }
}
