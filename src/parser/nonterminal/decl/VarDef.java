package parser.nonterminal.decl;

import lexer.Ident;
import parser.nonterminal.exp.Exp;

import java.util.List;
import java.util.Optional;

public class VarDef extends Def{

    public VarDef(Ident ident, List<Exp> constExps, InitVal initVal) {
        super(ident, constExps, initVal);
    }

    public VarDef(Ident ident, List<Exp> constExps) {
        super(ident, constExps, null);
    }

    public Optional<InitVal> getInitVal() {
        return Optional.ofNullable(initVal);
    }
}
