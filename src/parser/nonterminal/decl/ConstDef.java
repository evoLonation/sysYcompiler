package parser.nonterminal.decl;

import lexer.Ident;
import parser.nonterminal.exp.Exp;

import java.util.List;

public class ConstDef extends Def{
    public ConstDef(Ident ident, List<Exp> constExps, InitVal initVal) {
        super(ident, constExps, initVal);
    }

    public InitVal getInitVal() {
        return initVal;
    }
}
