package parser.nonterminal.stmt;

import parser.nonterminal.exp.LVal;

public class GetInt implements Stmt {
    private final LVal lVal;

    public GetInt(LVal lVal) {
        this.lVal = lVal;
    }

    public LVal getLVal() {
        return lVal;
    }
}
