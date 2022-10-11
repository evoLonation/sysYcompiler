package parser.nonterminal.decl;

import lexer.Ident;
import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;

import java.util.List;

public abstract class Def extends ASDDefault {
    protected final Ident ident;
    protected final List<Exp> constExps;
    protected final InitVal initVal;

    public Ident getIdent() {
        return ident;
    }

    public List<Exp> getConstExps() {
        return constExps;
    }

    protected Def(Ident ident, List<Exp> constExps, InitVal initVal) {
        this.ident = ident;
        this.constExps = constExps;
        this.initVal = initVal;
        addSon(constExps);
        addSon(initVal);
    }

}
