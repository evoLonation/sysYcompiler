package frontend.parser.nonterminal.exp;

import frontend.lexer.Ident;
import frontend.parser.nonterminal.AST;

import java.util.List;

public class LVal implements AST, Exp {
    private final Ident ident;
    private final List<Exp> exps;

    public LVal(Ident ident, List<Exp> exps) {
        this.ident = ident;
        this.exps = exps;
    }

    public Ident getIdent() {
        return ident;
    }

    public List<Exp> getExps() {
        return exps;
    }

}
