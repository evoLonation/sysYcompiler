package parser.nonterminal;

import lexer.Ident;
import parser.nonterminal.exp.Exp;

import java.util.List;

public class FuncDef {
    private boolean isInt;
    private Ident ident;
    private List<FuncFParam> funcFParams;
    private Block block;

    public FuncDef(boolean isInt, Ident ident, List<FuncFParam> funcFParams, Block block) {
        this.isInt = isInt;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
    }

    static public class FuncFParam{
        private Ident ident;
        private int dimension;
        private Exp constExp;

        public FuncFParam(Ident ident, int dimension, Exp constExp) {
            this.ident = ident;
            this.dimension = dimension;
            this.constExp = constExp;
        }
    }
}
