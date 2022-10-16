package parser.nonterminal.exp;

import lexer.TerminalType;
import parser.nonterminal.ASTDefault;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class UnaryExp extends ASTDefault implements Exp{
    private final UnaryOp op;
    private final Exp exp;

    public UnaryExp(UnaryOp op, Exp exp) {
        this.op = op;
        this.exp = exp;
        addSon(exp);
    }

    public UnaryOp getOp() {
        return op;
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
