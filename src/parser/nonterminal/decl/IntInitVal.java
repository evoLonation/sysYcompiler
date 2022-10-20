package parser.nonterminal.decl;

import parser.nonterminal.exp.Exp;
import type.VarType;


public class IntInitVal implements InitVal{
    private final Exp exp;

    public IntInitVal(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }

}
