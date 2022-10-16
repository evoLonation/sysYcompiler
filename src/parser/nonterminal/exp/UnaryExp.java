package parser.nonterminal.exp;

import lexer.TerminalType;
import parser.nonterminal.ASTDefault;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class UnaryExp extends ASTDefault implements Exp{
    private final List<TerminalType> UnaryOps;
    private final PrimaryExp primaryExp;

    public UnaryExp(List<TerminalType> unaryOps, PrimaryExp primaryExp) {
        UnaryOps = unaryOps;
        this.primaryExp = primaryExp;
        addSon(primaryExp);
    }

    public List<TerminalType> getUnaryOps() {
        return UnaryOps;
    }

    public PrimaryExp getPrimaryExp() {
        return primaryExp;
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
