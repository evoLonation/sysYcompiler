package parser.nonterminal.exp;

import lexer.Ident;
import parser.nonterminal.AST;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class FuncCall implements AST, Exp {
    private final Ident ident;
    private final List<Exp> exps;

    public FuncCall(Ident ident, List<Exp> exps) {
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
