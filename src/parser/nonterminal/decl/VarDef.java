package parser.nonterminal.decl;

import lexer.Ident;
import parser.nonterminal.exp.Exp;

import java.util.List;
import java.util.Optional;

public class VarDef extends Def{
    private final Optional<InitVal> initVal;

    public VarDef(Ident ident, List<Exp> constExps, InitVal initVal) {
        super(ident, constExps);
        this.initVal = Optional.of(initVal);
        addSon(initVal);
        addSon(constExps);
    }

    public VarDef(Ident ident, List<Exp> constExps) {
        super(ident, constExps);
        this.initVal = Optional.empty();
        addSon(constExps);
    }

    public Optional<InitVal> getInitVal() {
        return initVal;
    }
}
