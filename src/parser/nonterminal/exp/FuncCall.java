package parser.nonterminal.exp;

import lexer.Ident;
import parser.nonterminal.ASDDefault;
import type.Type;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class FuncCall extends ASDDefault implements PrimaryExp {
    private final Ident ident;
    private final List<Exp> exps;

    public FuncCall(Ident ident, List<Exp> exps) {
        this.ident = ident;
        this.exps = exps;
        addSon(exps);
    }

    public Ident getIdent() {
        return ident;
    }

    public List<Exp> getExps() {
        return exps;
    }

    private Optional<VarType> type = Optional.empty();

    public void setType(VarType type) {
        this.type = Optional.of(type);
    }

    @Override
    public Optional<VarType> getType() {
        return type;
    }
}
