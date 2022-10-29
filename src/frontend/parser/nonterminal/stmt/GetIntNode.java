package frontend.parser.nonterminal.stmt;

import frontend.parser.nonterminal.exp.LVal;

public class GetIntNode implements Stmt {
    private final LVal lVal;

    public GetIntNode(LVal lVal) {
        this.lVal = lVal;
    }

    public LVal getLVal() {
        return lVal;
    }
}
