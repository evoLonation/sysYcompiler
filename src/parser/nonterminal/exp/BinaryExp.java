package parser.nonterminal.exp;

import lexer.TerminalType;

import java.util.List;

public class BinaryExp implements Exp{
    private Exp first;
    private List<Exp> exps;
    private List<TerminalType> ops;

    public BinaryExp(Exp first, List<Exp> exps, List<TerminalType> ops) {
        this.first = first;
        this.exps = exps;
        this.ops = ops;
    }
}
