package parser.nonterminal.stmt;

import lexer.FormatString;
import parser.nonterminal.exp.Exp;

import java.util.List;

public class Printf implements Stmt {
    private final FormatString formatString;
    private final List<Exp> exps;
    private final int line;

    public Printf(FormatString formatString, List<Exp> exps, int line) {
        this.formatString = formatString;
        this.exps = exps;
        this.line = line;
    }

    public FormatString getFormatString() {
        return formatString;
    }

    public List<Exp> getExps() {
        return exps;
    }

    public int getLine() {
        return line;
    }
}
