package parser.nonterminal.exp;

import lexer.Ident;

import java.util.List;

public class LVal implements PrimaryExp {
    private Ident ident;
    private List<Exp> exps;

    public LVal(Ident ident, List<Exp> exps) {
        this.ident = ident;
        this.exps = exps;
    }
}
