package parser.nonterminal.decl;

import parser.nonterminal.ASD;
import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;
import type.VarType;

import java.util.List;

public class IntInitVal extends ASDDefault implements InitVal{
    private final Exp exp;

    public IntInitVal(Exp exp) {
        this.exp = exp;
        addSon(exp);
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public int getNumber() {
        return 1;
    }

    private VarType type;

    public void setType(VarType type) {
        this.type = type;
    }

    @Override
    public VarType getType() {
        return type;
    }
}
