package parser.nonterminal.decl;

import parser.nonterminal.ASDDefault;
import type.Type;
import type.VarType;

import java.util.List;

public class ArrayInitVal extends ASDDefault implements InitVal {
    private List<InitVal> initVals;

    public List<InitVal> getInitVals() {
        return initVals;
    }

    public ArrayInitVal(List<InitVal> initVals) {
        this.initVals = initVals;
        addSon(initVals);
    }

    private VarType type;

    @Override
    public VarType getType() {
        return type;
    }

    public void setType(VarType type) {
        this.type = type;
    }

    @Override
    public int getNumber() {
        return initVals.size();
    }
}