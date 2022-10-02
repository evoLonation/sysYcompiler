package parser.nonterminal.stmt;

import parser.nonterminal.exp.LVal;

public class GetInt implements Stmt {
    private LVal lVal;

    public GetInt(LVal lVal) {
        this.lVal = lVal;
    }
}
