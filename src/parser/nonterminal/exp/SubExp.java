package parser.nonterminal.exp;

import parser.nonterminal.ASD;
import parser.nonterminal.ASDDefault;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class SubExp extends ASDDefault implements PrimaryExp{
    private final Exp exp;

    public SubExp(Exp exp) {
        this.exp = exp;
        addSon(exp);
    }

    public Exp getExp() {
        return exp;
    }
    private Optional<VarType> type = Optional.empty();

    public void setType(VarType type) {
        this.type = Optional.of(type);
    }
    @Override
    public Optional<VarType> getOptionType() {
        return type;
    }
}
