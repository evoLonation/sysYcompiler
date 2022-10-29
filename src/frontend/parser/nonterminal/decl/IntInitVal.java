package frontend.parser.nonterminal.decl;

import frontend.parser.nonterminal.exp.Exp;


public class IntInitVal implements InitVal{
    private final Exp exp;

    public IntInitVal(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }

}
