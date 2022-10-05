package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;

import java.util.Optional;

public class Return extends ASDDefault implements Stmt {
    private final Optional<Exp> exp;
    private final int line;

    public Return(Exp exp, int line) {
        this.line = line;
        this.exp = Optional.of(exp);
        addSon(exp);
    }
    public Return(int line) {
        this.line = line;
        this.exp = Optional.empty();
    }

    public Optional<Exp> getExp() {
        return exp;
    }

    public int line() {
        return line;
    }
}
