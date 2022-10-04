package parser.nonterminal.decl;

import lexer.Ident;
import parser.nonterminal.exp.Exp;

import java.util.List;

public class ConstDef extends Def{
    private final InitVal initVal;

    public ConstDef(Ident ident, List<Exp> constExps, InitVal initVal) {
        super(ident, constExps);
        this.initVal = initVal;
        addSon(initVal);
        addSon(constExps);
    }

    public InitVal getInitVal() {
        return initVal;
    }
}
