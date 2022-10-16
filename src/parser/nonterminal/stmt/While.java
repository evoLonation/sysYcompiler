package parser.nonterminal.stmt;

import parser.nonterminal.ASTDefault;
import parser.nonterminal.exp.Exp;

import java.util.Optional;

public class While extends ASTDefault implements Stmt {
    private final Exp cond;
    private final Stmt stmt;

    public While(Exp cond, Stmt stmt) {
        this.cond = cond;
        this.stmt = stmt;
        addSon(cond, stmt);
    }
    public While(Exp cond) {
        this(cond, null);
    }

    public Exp getCond() {
        return cond;
    }

    public Optional<Stmt> getStmt() {
        return Optional.ofNullable(stmt);
    }
}
