package parser.nonterminal.exp;

import lexer.TerminalType;

import java.util.List;

public class UnaryExp implements Exp{
    private List<TerminalType> UnaryOps;
    private PrimaryExp primaryExp;

    public UnaryExp(List<TerminalType> unaryOps, PrimaryExp primaryExp) {
        UnaryOps = unaryOps;
        this.primaryExp = primaryExp;
    }
}
