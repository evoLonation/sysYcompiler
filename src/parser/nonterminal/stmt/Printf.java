package parser.nonterminal.stmt;

import lexer.FormatString;
import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;

import java.util.List;

public class Printf extends ASDDefault implements Stmt {
    private FormatString formatString;
    private List<Exp> exps;

    public Printf(FormatString formatString, List<Exp> exps) {
        this.formatString = formatString;
        this.exps = exps;
        addSon(exps);
    }
}
