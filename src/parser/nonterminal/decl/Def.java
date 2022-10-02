package parser.nonterminal.decl;

import lexer.Ident;
import parser.nonterminal.exp.Exp;

import java.util.List;

public class Def {
    Ident ident;
    List<Exp> constExps;
    InitVal initVal;


    public Def(Ident ident, List<Exp> constExps, InitVal initVal) {
        this.ident = ident;
        this.constExps = constExps;
        this.initVal = initVal;
    }

    static public interface InitVal{
    }

    static public class ArrayInitVal implements InitVal {
        private List<InitVal> initVals;

        public ArrayInitVal(List<InitVal> initVals) {
            this.initVals = initVals;
        }
    }
}
