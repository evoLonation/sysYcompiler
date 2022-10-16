package parser.nonterminal.stmt;

import parser.nonterminal.ASTDefault;
import parser.nonterminal.exp.Exp;

import java.util.Optional;

public class Return extends ASTDefault implements Stmt {
    private final Exp exp;
    private final int line;

    public Return(Exp exp, int line) {
        this.line = line;
        this.exp = exp;
        addSon(exp);
    }
    public Return(int line) {
        this(null, line);
    }

    public Optional<Exp> getExp() {
        return Optional.ofNullable(exp);
    }

    public int line() {
        return line;
    }
}
