package frontend.parser.nonterminal.decl;

import java.util.List;

public class ArrayInitVal implements InitVal {
    private final List<InitVal> initVals;

    public List<InitVal> getInitVals() {
        return initVals;
    }

    public ArrayInitVal(List<InitVal> initVals) {
        this.initVals = initVals;
    }

}