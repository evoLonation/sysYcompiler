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
    private VarType type;

    public void setType(VarType type) {
        this.type = type;
    }

    @Override
    public Optional<VarType> getOptionType() {
        return Optional.ofNullable(type);
    }
}
