package parser.nonterminal.decl;

import lexer.Ident;
import parser.nonterminal.ASD;
import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;
import type.Type;

import java.util.List;

public abstract class Def extends ASDDefault {
    Ident ident;
    List<Exp> constExps;

    public Ident getIdent() {
        return ident;
    }

    public List<Exp> getConstExps() {
        return constExps;
    }

    public Def(Ident ident, List<Exp> constExps) {
        this.ident = ident;
        this.constExps = constExps;
        addSon(constExps);
    }

}
