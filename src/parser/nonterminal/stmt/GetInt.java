package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.LVal;

public class GetInt extends ASDDefault implements Stmt {
    private final LVal lVal;

    public GetInt(LVal lVal) {
        this.lVal = lVal;
        addSon(lVal);
    }

    public LVal getLVal() {
        return lVal;
    }
}
