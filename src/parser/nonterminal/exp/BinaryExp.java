package parser.nonterminal.exp;

import lexer.TerminalType;
import parser.nonterminal.ASDDefault;
import type.VarType;

import java.util.List;
import java.util.Optional;


public class BinaryExp extends ASDDefault implements Exp{
    private final Exp first;
    private final List<Exp> exps;
    private final List<TerminalType> ops;

    private final ExpLayer layer;

    public BinaryExp(Exp first, List<Exp> exps, List<TerminalType> ops, ExpLayer layer) {
        this.first = first;
        this.exps = exps;
        this.ops = ops;
        this.layer = layer;
        addSon(first);
        addSon(exps);
    }

    public ExpLayer getLayer() {
        return layer;
    }

    public Exp getFirst() {
        return first;
    }

    public List<Exp> getExps() {
        return exps;
    }

    public List<TerminalType> getOps() {
        return ops;
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
