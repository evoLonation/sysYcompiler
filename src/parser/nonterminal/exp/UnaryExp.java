package parser.nonterminal.exp;

public class UnaryExp implements Exp{
    private final UnaryOp op;
    private final Exp exp;

    public UnaryExp(UnaryOp op, Exp exp) {
        this.op = op;
        this.exp = exp;
    }

    public UnaryOp getOp() {
        return op;
    }

    public Exp getExp() {
        return exp;
    }

}
