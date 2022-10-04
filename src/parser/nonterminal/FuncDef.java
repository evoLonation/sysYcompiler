package parser.nonterminal;

import lexer.Ident;
import parser.nonterminal.exp.Exp;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class FuncDef extends ASDDefault implements ASD{
    private boolean isInt;
    private Ident ident;
    private List<FuncFParam> funcFParams;
    private List<BlockItem> blockItems;

    public FuncDef(boolean isInt, Ident ident, List<FuncFParam> funcFParams, List<BlockItem> blockItems) {
        this.isInt = isInt;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.blockItems = blockItems;
        addSon(funcFParams);
        addSon(blockItems);
    }

    public boolean isInt() {
        return isInt;
    }

    public Ident getIdent() {
        return ident;
    }

    public List<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    public List<BlockItem> getBlockItems() {
        return blockItems;
    }

    static public class FuncFParam extends ASDDefault {
        private final Ident ident;
        private final int dimension;
        private Optional<Exp> constExp;

        public FuncFParam(Ident ident, int dimension, Exp constExp) {
            this.ident = ident;
            this.dimension = dimension;
            this.constExp = Optional.of(constExp);
            addSon(constExp);
        }
        public FuncFParam(Ident ident, int dimension) {
            this.ident = ident;
            this.dimension = dimension;
        }

        public Ident getIdent() {
            return ident;
        }

        public int getDimension() {
            return dimension;
        }

        public Optional<Exp> getConstExp() {
            return constExp;
        }

        private VarType type;

        public VarType getType() {
            return type;
        }

        public void setType(VarType type) {
            this.type = type;
        }
    }
}
