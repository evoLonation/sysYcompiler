package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;

import java.util.Optional;

public class While extends ASDDefault implements Stmt {
    private final Exp cond;
    private final Optional<Stmt> stmt;

    public While(Exp cond, Optional<Stmt> stmt) {
        this.cond = cond;
        this.stmt = stmt;
        addSon(cond);
        stmt.ifPresent(this::addSon);
    }

    public Exp getCond() {
        return cond;
    }

    public Optional<Stmt> getStmt() {
        return stmt;
    }
}
