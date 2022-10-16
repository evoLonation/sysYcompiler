package parser.nonterminal.exp;

import lexer.Ident;
import parser.nonterminal.AST;
import parser.nonterminal.ASTDefault;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class LVal extends ASTDefault implements AST, ExpTyper, Exp {
    private final Ident ident;
    private final List<Exp> exps;

    public LVal(Ident ident, List<Exp> exps) {
        this.ident = ident;
        this.exps = exps;
        addSon(exps);
    }

    public VarType getType() {
        return type;
    }

    public Ident getIdent() {
        return ident;
    }

    public List<Exp> getExps() {
        return exps;
    }

    private VarType type;

    public void setType(VarType type) {
        this.type = type;
    }
    @Override
    public Optional<VarType> getOptionType() {
        return Optional.of(type);
    }
}
