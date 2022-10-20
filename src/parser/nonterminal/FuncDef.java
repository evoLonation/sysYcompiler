package parser.nonterminal;

import lexer.Ident;
import parser.nonterminal.exp.Exp;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class FuncDef implements AST {
    private final boolean isInt;
    private final Ident ident;
    private final List<FuncFParam> funcFParams;
    private final Block block;

    public FuncDef(boolean isInt, Ident ident, List<FuncFParam> funcFParams, Block block) {
        this.isInt = isInt;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
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

    public Block getBlock() {
        return block;
    }

    static public class FuncFParam {
        private final Ident ident;
        private final int dimension;
        private Exp constExp;

        public FuncFParam(Ident ident, int dimension, Exp constExp) {
            this.ident = ident;
            this.dimension = dimension;
            this.constExp = constExp;
        }
        public FuncFParam(Ident ident, int dimension) {
            this(ident, dimension, null);
        }

        public Ident getIdent() {
            return ident;
        }

        public int getDimension() {
            return dimension;
        }

        public Optional<Exp> getConstExp() {
            return Optional.ofNullable(constExp);
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
