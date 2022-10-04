package parser.nonterminal.exp;

import lexer.TerminalType;
import parser.nonterminal.ASDDefault;
import type.Type;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class UnaryExp extends ASDDefault implements Exp{
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

    private Optional<VarType> type = Optional.empty();

    public void setType(VarType type) {
        this.type = Optional.of(type);
    }
    @Override
    public Optional<VarType> getType() {
        return type;
    }

}
