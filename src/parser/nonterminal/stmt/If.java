package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;

import java.util.Optional;

public class If extends ASDDefault implements Stmt {
    private final Exp cond;
    private final Optional<Stmt> ifStmt;
    private final Optional<Stmt> elseStmt;

    public If(Exp cond, Optional<Stmt> ifStmt, Optional<Stmt> elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
        addSon(cond);
        ifStmt.ifPresent(this::addSon);
        elseStmt.ifPresent(this::addSon);
    }

    public Exp getCond() {
        return cond;
    }

    public Optional<Stmt> getIfStmt() {
        return ifStmt;
    }

    public Optional<Stmt> getElseStmt() {
        return elseStmt;
    }
}

