package parser.nonterminal.stmt;

import parser.nonterminal.exp.Exp;

import java.util.Optional;

public class ReturnNode implements Stmt {
    private final Exp exp;
    private final int line;

    public ReturnNode(Exp exp, int line) {
        this.line = line;
        this.exp = exp;
    }
    public ReturnNode(int line) {
        this(null, line);
    }

    public Optional<Exp> getExp() {
        return Optional.ofNullable(exp);
    }

    public int line() {
        return line;
    }
}
