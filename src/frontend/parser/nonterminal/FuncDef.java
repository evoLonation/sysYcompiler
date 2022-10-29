package frontend.parser.nonterminal;

import frontend.lexer.Ident;
import frontend.parser.nonterminal.exp.Exp;

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

    static public abstract class FuncFParam {
        private final Ident ident;

        public FuncFParam(Ident ident) {
            this.ident = ident;
        }

        public Ident getIdent() {
            return ident;
        }
    }

    static public class IntFParam extends FuncFParam {
        public IntFParam(Ident ident) {
            super(ident);
        }
    }

    static public class PointerFParam extends FuncFParam{
        private final Exp constExp;
        public Optional<Exp> getConstExp() {
            return Optional.ofNullable(constExp);
        }

        public PointerFParam(Ident ident, Exp constExp) {
            super(ident);
            this.constExp = constExp;
        }
        public PointerFParam(Ident ident) {
            super(ident);
            this.constExp = null;
        }
    }
}
