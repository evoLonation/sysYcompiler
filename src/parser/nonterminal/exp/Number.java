package parser.nonterminal.exp;

import lexer.IntConst;

public class Number implements PrimaryExp {
    private IntConst intConst;

    public Number(IntConst intConst) {
        this.intConst = intConst;
    }
}
