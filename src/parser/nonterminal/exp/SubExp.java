package parser.nonterminal.exp;

import parser.nonterminal.ASTDefault;
import type.VarType;

import java.util.Optional;

public class SubExp extends ASTDefault implements PrimaryExp{
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
