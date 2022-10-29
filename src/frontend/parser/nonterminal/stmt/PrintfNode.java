package frontend.parser.nonterminal.stmt;

import frontend.lexer.FormatString;
import frontend.parser.nonterminal.exp.Exp;

import java.util.List;

public class PrintfNode implements Stmt {
    private final FormatString formatString;
    private final List<Exp> exps;
    private final int line;

    public PrintfNode(FormatString formatString, List<Exp> exps, int line) {
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
