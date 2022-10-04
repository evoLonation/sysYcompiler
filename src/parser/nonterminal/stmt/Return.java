package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;

public class Return extends ASDDefault implements Stmt {
    private Exp exp;

    public Return(Exp exp) {
        this.exp = exp;
        addSon(exp);
    }
}
