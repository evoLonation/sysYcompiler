package frontend.parser.nonterminal.exp;

import frontend.lexer.IntConst;
import frontend.parser.nonterminal.AST;

public class Number implements AST, Exp {
    private final int number;

    public Number(IntConst intConst) {
        number = intConst.getDigitValue();
    }

    public Number(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
